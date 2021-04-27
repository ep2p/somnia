package io.ep2p.somnia.decentralized;

import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.model.FindNodeAnswer;
import com.github.ep2p.kademlia.model.StoreAnswer;
import com.github.ep2p.kademlia.node.KademliaRepository;
import com.github.ep2p.kademlia.node.KademliaSyncRepositoryNode;
import com.github.ep2p.kademlia.node.Node;
import com.github.ep2p.kademlia.table.Bucket;
import com.github.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Optional;

@Slf4j
public class SomniaKademliaSyncRepositoryNode extends KademliaSyncRepositoryNode<BigInteger, SomniaConnectionInfo, SomniaKey, SomniaValue> {
    private final SomniaEntityManager somniaEntityManager;

    public SomniaKademliaSyncRepositoryNode(
            BigInteger nodeId,
            RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>>
                    routingTable,
            NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
            SomniaConnectionInfo connectionInfo,
            KademliaRepository<SomniaKey, SomniaValue> kademliaRepository,
            SomniaEntityManager somniaEntityManager) {
        super(
                nodeId,
                routingTable,
                nodeConnectionApi,
                connectionInfo,
                kademliaRepository,
                new SomniaKeyHashGenerator());
        this.somniaEntityManager = somniaEntityManager;
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
            // still, store the data
            getKademliaRepository().store(key, value);
            // but also pass data to the closest node to store it
            FindNodeAnswer<BigInteger, SomniaConnectionInfo> findNodeAnswer = this.getRoutingTable().findClosest(key.getHash());

        }
    }

    private void handleHitStore(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value) {
        if (this.getId().equals(key.getHash())) {
            // Hash is not bounded since both network size and keys are BigInteger type. If by any change they are equal, lets store the data
            doStore(requester, key, value);
        } else {
            // Otherwise, find the closest node possible to store the data
            // If no such a node was found, just store the data
            StoreAnswer<BigInteger, SomniaKey> result = storeInClosestNodes(requester, key, value, caller);
            if (result == null) {
                doStore(requester, key, value);
            }
        }
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

}
