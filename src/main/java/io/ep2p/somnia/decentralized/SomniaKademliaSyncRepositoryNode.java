package io.ep2p.somnia.decentralized;

import io.ep2p.kademlia.NodeSettings;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static io.ep2p.kademlia.util.DateUtil.getDateOfSecondsAgo;

@Slf4j
public class SomniaKademliaSyncRepositoryNode extends KademliaSyncRepositoryNode<BigInteger, SomniaConnectionInfo, SomniaKey, SomniaValue> {
    private final SomniaEntityManager somniaEntityManager;
    private final SomniaStorageConfig somniaStorageConfig;

    public SomniaKademliaSyncRepositoryNode(
            BigInteger nodeId,
            NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
            SomniaConnectionInfo connectionInfo,
            NodeSettings nodeSettings,
            KademliaRepository<SomniaKey, SomniaValue> kademliaRepository,
            SomniaEntityManager somniaEntityManager) {
        this(nodeId, nodeConnectionApi, connectionInfo, nodeSettings, kademliaRepository, somniaEntityManager, new SomniaStorageConfig());
    }

    public SomniaKademliaSyncRepositoryNode(
            BigInteger nodeId,
            NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
            SomniaConnectionInfo connectionInfo,
            NodeSettings nodeSettings,
            KademliaRepository<SomniaKey, SomniaValue> kademliaRepository,
            SomniaEntityManager somniaEntityManager, SomniaStorageConfig somniaStorageConfig) {
        super(
                nodeId,
                nodeConnectionApi,
                connectionInfo,
                nodeSettings,
                kademliaRepository,
                new SomniaKeyHashGenerator());
        this.somniaEntityManager = somniaEntityManager;
        this.somniaStorageConfig = somniaStorageConfig;
    }

    public SomniaKademliaSyncRepositoryNode(
            BigInteger nodeId,
            RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>>
                    routingTable,
            NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
            SomniaConnectionInfo connectionInfo,
            NodeSettings nodeSettings,
            KademliaRepository<SomniaKey, SomniaValue> kademliaRepository,
            SomniaEntityManager somniaEntityManager) {
        this(nodeId, routingTable,nodeConnectionApi, connectionInfo, nodeSettings, kademliaRepository, somniaEntityManager, new SomniaStorageConfig());
    }

    public SomniaKademliaSyncRepositoryNode(
            BigInteger nodeId,
            RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>>
                    routingTable,
            NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
            SomniaConnectionInfo connectionInfo,
            NodeSettings nodeSettings,
            KademliaRepository<SomniaKey, SomniaValue> kademliaRepository,
            SomniaEntityManager somniaEntityManager, SomniaStorageConfig somniaStorageConfig) {
        super(
                nodeId,
                routingTable,
                nodeConnectionApi,
                connectionInfo,
                nodeSettings,
                kademliaRepository,
                new SomniaKeyHashGenerator());
        this.somniaEntityManager = somniaEntityManager;
        this.somniaStorageConfig = somniaStorageConfig;
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
        Date date = getDateOfSecondsAgo(getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());

        /*
         * Look for nodes with the data.
         * From the second time we look through the alive close nodes, make sure number of times we look for it won't pass 1/4 of minimum distribution
         */

        boolean firstLoopDone = false;
        for (ExternalNode<BigInteger, SomniaConnectionInfo> externalNode : findNodeAnswer.getNodes()) {
            if(key.getDistributions() > (somniaStorageConfig.getMaximumDistribution() / somniaStorageConfig.getPerNodeDistribution()) && firstLoopDone){
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
            log.debug("Already having the key " + key.getKey() + " in node " + this.getId());
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

        BigInteger hash = hash(key);

        ArrayList<ExternalNode<BigInteger, SomniaConnectionInfo>> nodes = this.getRoutingTable().findClosest(hash).getNodes();
        if (key.getDistributions() % 2 == 0){
            Collections.reverse(nodes);
        }

        int d = doDataDistribution(nodes, requester, key, value, nodeToIgnore, 0);
        if (d < somniaStorageConfig.getPerNodeDistribution()){
            nodes = this.getRoutingTable().findClosest(hash.xor(this.getId())).getNodes();
            d = doDataDistribution(nodes, requester, key, value, nodeToIgnore, d);
        }
        if (d < somniaStorageConfig.getPerNodeDistribution()){
            nodes = this.getRoutingTable().findClosest(hash.xor(BigInteger.valueOf((long) Math.pow(2, getNodeSettings().getIdentifierSize())))).getNodes();
            doDataDistribution(nodes, requester, key, value, nodeToIgnore, d);
        }

    }

    protected int doDataDistribution(ArrayList<ExternalNode<BigInteger, SomniaConnectionInfo>> nodes, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value, Node<BigInteger, SomniaConnectionInfo> nodeToIgnore, int distribution){
        Date date = getDateOfSecondsAgo(getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());

        for (ExternalNode<BigInteger, SomniaConnectionInfo> externalNode: nodes) {
            //skip current node
            if(externalNode.getId().equals(getId())){
                continue;
            }
            // skip the ignored node
            if(nodeToIgnore != null && nodeToIgnore.getId().equals(externalNode.getId())){
                continue;
            }
            // if minimum distribution has reached, there is no need to distribute more
            if (key.getDistributions() == this.somniaStorageConfig.getMaximumDistribution() || distribution == somniaStorageConfig.getPerNodeDistribution()){
                break;
            }

            log.debug("Distributing store for key " + key.getKey() + " in node " + this.getId() + " to node " + externalNode.getId() + ". Distribution: " + key.getDistributions());
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
        return distribution;
    }

}
