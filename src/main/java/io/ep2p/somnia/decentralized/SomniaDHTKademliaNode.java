package io.ep2p.somnia.decentralized;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.exception.HandlerNotFoundException;
import io.ep2p.kademlia.model.FindNodeAnswer;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.DHTKademliaNode;
import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.protocol.message.DHTLookupKademliaMessage;
import io.ep2p.kademlia.protocol.message.DHTStoreKademliaMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PingKademliaMessage;
import io.ep2p.kademlia.repository.KademliaRepository;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.EntityType;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static io.ep2p.kademlia.util.DateUtil.getDateOfSecondsAgo;


@Slf4j
public class SomniaDHTKademliaNode extends DHTKademliaNode<BigInteger, SomniaConnectionInfo, SomniaKey, SomniaValue> {
    private final SomniaEntityManager somniaEntityManager;
    private final SomniaStorageConfig somniaStorageConfig;

    public SomniaDHTKademliaNode(BigInteger bigInteger, SomniaConnectionInfo connectionInfo, RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>> routingTable, MessageSender<BigInteger, SomniaConnectionInfo> messageSender, NodeSettings nodeSettings, KademliaRepository<SomniaKey, SomniaValue> kademliaRepository, KeyHashGenerator<BigInteger, SomniaKey> keyHashGenerator, SomniaEntityManager somniaEntityManager, SomniaStorageConfig somniaStorageConfig) {
        super(bigInteger, connectionInfo, routingTable, messageSender, nodeSettings, kademliaRepository, keyHashGenerator);
        this.somniaEntityManager = somniaEntityManager;
        this.somniaStorageConfig = somniaStorageConfig;
    }


    //// --------- LOOKUP --------- ////

    @Override
    protected LookupAnswer<BigInteger, SomniaKey, SomniaValue> handleLookup(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, int currentTry) {
        Optional<SomniaDocument> optionalSomniaDocument = somniaEntityManager.getDocumentOfName(key.getName());
        if(!optionalSomniaDocument.isPresent()){
            return getNewLookupAnswer(key, LookupAnswer.Result.FAILED, this, null);
        }

        if (optionalSomniaDocument.get().type().equals(EntityType.HIT)){
            return super.handleLookup(caller, requester, key, currentTry);
        }

        return handleDistributedLookup(caller, requester, key, currentTry);
    }

    private LookupAnswer<BigInteger, SomniaKey, SomniaValue> handleDistributedLookup(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, int currentTry) {
        if (getKademliaRepository().contains(key)) {
            return getNewLookupAnswer(key, LookupAnswer.Result.FOUND, this, getKademliaRepository().get(key));
        }

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

            KademliaMessage<BigInteger, SomniaConnectionInfo, ?> pingAnswer;
            if(externalNode.getLastSeen().before(date) || (pingAnswer = getMessageSender().sendMessage(this, externalNode, new PingKademliaMessage<>())).isAlive()){
                getMessageSender().sendAsyncMessage(
                        this,
                        externalNode,
                        new DHTLookupKademliaMessage<>(
                                new DHTLookupKademliaMessage.DHTLookup<>(requester, key, currentTry + 1)
                        )
                );
                firstLoopDone = true;

                //otherwise remove the node from routing table, since its offline
            }else if(!pingAnswer.isAlive()){
                getRoutingTable().delete(externalNode);
            }
        }
        return getNewLookupAnswer(key, LookupAnswer.Result.PASSED, this, null);
    }


    //// --------- STORE --------- ////

    protected StoreAnswer<BigInteger, SomniaKey> handleStore(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value){
        Optional<SomniaDocument> optionalSomniaDocument = somniaEntityManager.getDocumentOfName(key.getName());
        if (!optionalSomniaDocument.isPresent()){
            return getNewStoreAnswer(key, StoreAnswer.Result.FAILED, this);
        } else {
            switch (optionalSomniaDocument.get().type()) {
                case HIT:
                    return super.handleStore(caller, requester, key, value);
                case DISTRIBUTE:
                    return handleDistributedStore(caller, requester, key, value);
            }
            return getNewStoreAnswer(key, StoreAnswer.Result.FAILED, this);
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

    protected void distributeDataToOtherNodes(Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value, Node<BigInteger, SomniaConnectionInfo> nodeToIgnore){
        BigInteger hash = hash(key);

        List<ExternalNode<BigInteger, SomniaConnectionInfo>> nodes = this.getRoutingTable().findClosest(hash).getNodes();
        if (key.getDistributions() % 2 == 0){
            Collections.reverse(nodes);
        }

        int d = doDataDistribution(nodes, requester, key, value, nodeToIgnore, 0);
        if (d < somniaStorageConfig.getPerNodeDistribution()){
            nodes = this.getRoutingTable().findClosest(hash.xor(this.getId())).getNodes();
            d = doDataDistribution(nodes, requester, key, value, nodeToIgnore, d);
        }
        if (d < somniaStorageConfig.getPerNodeDistribution()){
            nodes = this.getRoutingTable().findClosest(
                    hash.xor(
                            BigInteger.valueOf(
                                    (long) Math.pow(2, getNodeSettings().getIdentifierSize())
                            )
                    )
            ).getNodes();
            doDataDistribution(nodes, requester, key, value, nodeToIgnore, d);
        }
    }

    protected int doDataDistribution(List<ExternalNode<BigInteger, SomniaConnectionInfo>> nodes, Node<BigInteger, SomniaConnectionInfo> requester, SomniaKey key, SomniaValue value, Node<BigInteger, SomniaConnectionInfo> nodeToIgnore, int distribution){
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

            KademliaMessage<BigInteger, SomniaConnectionInfo, ?> pingAnswer;

            if(externalNode.getLastSeen().after(date) || (pingAnswer = getMessageSender().sendMessage(this, externalNode, new PingKademliaMessage<>())).isAlive()){
                getMessageSender().sendAsyncMessage(
                        this,
                        externalNode,
                        new DHTStoreKademliaMessage<>(
                                new DHTStoreKademliaMessage.DHTData<>(requester, key, value)
                        )
                );
            }else {
                // We have definitely pinged the node, lets handle the pong specially now that node is offline
                try {
                    onMessage(pingAnswer);
                } catch (HandlerNotFoundException e) {
                    // Should not get stuck here. Main objective is to store the message
                    log.error(e.getMessage(), e);
                }
            }
        }
        return distribution;
    }

}
