package io.ep2p.somnia.decentralized;

import io.ep2p.kademlia.connection.NodeConnectionApi;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.PingAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.KademliaRepository;
import io.ep2p.kademlia.node.KademliaSyncRepositoryNode;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

import static io.ep2p.kademlia.Common.LAST_SEEN_SECONDS_TO_CONSIDER_ALIVE;
import static io.ep2p.kademlia.util.DateUtil.getDateOfSecondsAgo;

@Slf4j
public class SomniaKademliaSyncRepositoryNode extends KademliaSyncRepositoryNode<BigInteger, SomniaConnectionInfo, SomniaKey, SomniaValue> {
    private final SomniaEntityManager somniaEntityManager;
    private final Config config;

    public SomniaKademliaSyncRepositoryNode(
            BigInteger nodeId,
            RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>>
                    routingTable,
            NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
            SomniaConnectionInfo connectionInfo,
            KademliaRepository<SomniaKey, SomniaValue> kademliaRepository,
            SomniaEntityManager somniaEntityManager) {
        this(nodeId, routingTable,nodeConnectionApi, connectionInfo, kademliaRepository, somniaEntityManager, new Config());
    }

    public SomniaKademliaSyncRepositoryNode(
            BigInteger nodeId,
            RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>>
                    routingTable,
            NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
            SomniaConnectionInfo connectionInfo,
            KademliaRepository<SomniaKey, SomniaValue> kademliaRepository,
            SomniaEntityManager somniaEntityManager, Config config) {
        super(
                nodeId,
                routingTable,
                nodeConnectionApi,
                connectionInfo,
                kademliaRepository,
                new SomniaKeyHashGenerator());
        this.somniaEntityManager = somniaEntityManager;
        this.config = config;
    }

    @Override
    public void onGetRequest(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key) {
        Optional<SomniaDocument> optionalSomniaDocument = somniaEntityManager.getDocumentOfName(key.getName());
        if(!optionalSomniaDocument.isPresent()){
            this.getNodeConnectionApi().sendGetResults(this, requester, key, null);
        }else {
            handleGetRequest(caller, requester, key, optionalSomniaDocument.get());
        }
    }

    private void handleGetRequest(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaDocument somniaDocument) {
        if (getKademliaRepository().contains(key)) {
            this.getNodeConnectionApi().sendGetResults(this, requester, key, getKademliaRepository().get(key));
            return;
        }
        switch (somniaDocument.type()){
            case HIT:
                handleHitGet(caller, requester, key);
                break;
            case DISTRIBUTE:
                handleDistributedGet(caller, requester, key);
                break;
        }
    }

    private void handleHitGet(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key) {
        GetAnswer<BigInteger, SomniaKey, SomniaValue> getAnswer = getDataFromClosestNodes(requester, key, caller);
        if(getAnswer == null)
            getNodeConnectionApi().sendGetResults(this, requester, key, null);
    }

    private void handleDistributedGet(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key) {
        key.setHitNode(null);
        BigInteger hash = hash(key);
        FindNodeAnswer<BigInteger, SomniaConnectionInfo> findNodeAnswer = getRoutingTable().findClosest(hash);
        Date date = getDateOfSecondsAgo(LAST_SEEN_SECONDS_TO_CONSIDER_ALIVE);

        /*
         * Look for nodes with the data.
         * From the second time we look through the alive close nodes, make sure number of times we look for it won't pass 1/4 of minimum distribution
         */

        boolean firstLoopDone = false;
        for (ExternalNode<BigInteger, SomniaConnectionInfo> externalNode : findNodeAnswer.getNodes()) {
            if(key.getDistributions() > (config.getMinimumDistribution() / 4) && firstLoopDone){
                break;
            }
            if(firstLoopDone){
                key.incrementDistribution();
            }

            if(externalNode.getId().equals(getId()) || (externalNode.getId().equals(caller.getId())))
                continue;

            PingAnswer<BigInteger> pingAnswer;
            if(externalNode.getLastSeen().before(date) || (pingAnswer = getNodeConnectionApi().ping(this, externalNode)).isAlive()){
                getNodeConnectionApi().getRequest(this, requester, externalNode, key);
                firstLoopDone = true;
            }else if(!pingAnswer.isAlive()){
                getRoutingTable().delete(externalNode);
            }
        }
    }

    @Override
    protected StoreAnswer<BigInteger, SomniaKey> handleStore(SomniaKey key, SomniaValue value, boolean force, Node<BigInteger, SomniaConnectionInfo> requester, Node<BigInteger, SomniaConnectionInfo> caller) throws StoreException {
        Optional<SomniaDocument> optionalSomniaDocument = somniaEntityManager.getDocumentOfName(key.getName());
        if(!optionalSomniaDocument.isPresent()){
            return getNewStoreAnswer(key, StoreAnswer.Result.FAILED, this);
        }else {
            return handleStoreRequest(caller, requester, key, value, optionalSomniaDocument.get());
        }
    }

    private StoreAnswer<BigInteger, SomniaKey> handleStoreRequest(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value, SomniaDocument somniaDocument) {
        switch (somniaDocument.type()) {
            case HIT:
                return handleHitStore(caller, requester, key, value);
            case DISTRIBUTE:
                return handleDistributedStore(caller, requester, key, value);
        }
        return getNewStoreAnswer(key, StoreAnswer.Result.FAILED, this);
    }

    private StoreAnswer<BigInteger, SomniaKey> handleHitStore(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value) {
        if (this.getId().equals(key.getHash())) {
            doStore(key, value);
            return getNewStoreAnswer(key, StoreAnswer.Result.STORED, this);
        } else {
            // pass data to the closest node to store it
            return storeInClosestNodes(requester, key, value, caller);
        }
    }

    private StoreAnswer<BigInteger, SomniaKey> handleDistributedStore(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value) {
        if (getKademliaRepository().contains(key)){
            this.getNodeConnectionApi().sendStoreResults(this, requester, key, true);
            return getNewStoreAnswer(key, StoreAnswer.Result.STORED, this);
        }
        StoreAnswer<BigInteger, SomniaKey> storeAnswer = doStore(key, value);
        distributeDataToOtherNodes(requester, key, value, caller);
        return storeAnswer;
    }

    private StoreAnswer<BigInteger, SomniaKey> doStore(SomniaKey key, SomniaValue value) {
        try {
            getKademliaRepository().store(key, value);
            return getNewStoreAnswer(key, StoreAnswer.Result.STORED, this);
        }catch (Exception e){
            log.error("Failed to store data on kademlia. " + key, e);
            return getNewStoreAnswer(key, StoreAnswer.Result.FAILED, this);
        }
    }

    private StoreAnswer<BigInteger, SomniaKey> storeInClosestNodes(Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value, Node<BigInteger, SomniaConnectionInfo> caller){
        FindNodeAnswer<BigInteger, SomniaConnectionInfo> findNodeAnswer = this.getRoutingTable().findClosest(hash(key));
        return this.storeDataToClosestNode(requester, findNodeAnswer.getNodes(), key, value, caller);
    }

    protected void distributeDataToOtherNodes(Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value, Node<BigInteger, SomniaConnectionInfo> nodeToIgnore){
        Date date = getDateOfSecondsAgo(LAST_SEEN_SECONDS_TO_CONSIDER_ALIVE);
        FindNodeAnswer<BigInteger, SomniaConnectionInfo> findNodeAnswer = this.getRoutingTable().findClosest(hash(key));

        for (ExternalNode<BigInteger, SomniaConnectionInfo> externalNode : findNodeAnswer.getNodes()) {
            //skip current node
            if(externalNode.getId().equals(getId())){
                if (key.getDistributions() > 3){
                    break;
                }else {
                    continue;
                }
            }
            // skip the ignored node
            if(nodeToIgnore != null && nodeToIgnore.getId().equals(externalNode.getId())){
                continue;
            }
            // if minimum distribution has reached, there is no need to distribute more
            if (key.getDistributions() == this.config.getMinimumDistribution()){
                break;
            }

            //try next close requester in routing table
            PingAnswer<BigInteger> pingAnswer;
            //if external node is alive, tell it to store the data
            if(externalNode.getLastSeen().before(date) || (pingAnswer = getNodeConnectionApi().ping(this, externalNode)).isAlive()){
                key.incrementDistribution();
                getNodeConnectionApi().storeAsync(this, requester, externalNode, key, value);
            }else if(!pingAnswer.isAlive()){
                getRoutingTable().delete(externalNode);
            }
        }
    }

}
