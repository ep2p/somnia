package io.ep2p.somnia.decentralized;

import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.PingAnswer;
import com.github.ep2p.kademlia.model.StoreAnswer;
import com.github.ep2p.kademlia.node.KademliaRepository;
import com.github.ep2p.kademlia.node.KademliaSyncRepositoryNode;
import com.github.ep2p.kademlia.node.Node;
import com.github.ep2p.kademlia.node.external.ExternalNode;
import com.github.ep2p.kademlia.table.Bucket;
import com.github.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.github.ep2p.kademlia.Common.LAST_SEEN_SECONDS_TO_CONSIDER_ALIVE;
import static com.github.ep2p.kademlia.util.DateUtil.getDateOfSecondsAgo;

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
    public void onStoreRequest(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value) {
        Optional<SomniaDocument> optionalSomniaDocument = somniaEntityManager.getDocumentOfName(key.getName());
        if(!optionalSomniaDocument.isPresent()){
            this.getNodeConnectionApi().sendStoreResults(this, requester, key, false);
        }else {
            handleStoreRequest(caller, requester, key, value, optionalSomniaDocument.get());
        }
    }

    private void handleStoreRequest(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value, SomniaDocument somniaDocument) {
        switch (somniaDocument.type()) {
            case HIT:
                handleHitStore(caller, requester, key, value);
                break;
            case DISTRIBUTE:
                handleDistributedStore(caller, requester, key, value);
                break;
        }
    }

    private void handleDistributedStore(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value) {
        if (this.getId().equals(key.getHash())) {
            doStore(requester, key, value);
        } else {
            // pass data to the closest node to store it
            StoreAnswer<BigInteger, SomniaKey> storeAnswer = storeInClosestNodes(requester, key, value, caller);
            // still, store the data
            getKademliaRepository().store(key, value);
        }
    }

    private void handleHitStore(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value) {
        if (getKademliaRepository().contains(key))
            return;
        doStore(requester, key, value);
        distributeDataToOtherNodes(requester, key, value, caller);
    }

    private void doStore(Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value) {
        try {
            getKademliaRepository().store(key, value);
            this.getNodeConnectionApi().sendStoreResults(this, requester, key, true);
        }catch (Exception e){
            log.error("Failed to store data on kademlia. " + key, e);
            this.getNodeConnectionApi().sendStoreResults(this, requester, key, false);
        }
    }


    private StoreAnswer<BigInteger, SomniaKey> storeInClosestNodes(Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value, Node<BigInteger, SomniaConnectionInfo> caller){
        FindNodeAnswer<BigInteger, SomniaConnectionInfo> findNodeAnswer = this.getRoutingTable().findClosest(hash(key));
        return this.storeDataToClosestNode(requester, findNodeAnswer.getNodes(), key, value, caller);
    }



    protected void distributeDataToOtherNodes(Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value, Node<BigInteger, SomniaConnectionInfo> nodeToIgnore){
        Date date = getDateOfSecondsAgo(LAST_SEEN_SECONDS_TO_CONSIDER_ALIVE);
        StoreAnswer<BigInteger, SomniaKey> storeAnswer = null;

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
