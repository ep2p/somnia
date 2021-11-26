package io.ep2p.somnia.decentralized.protocol.handler;

import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.protocol.handler.MessageHandler;
import io.ep2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.kademlia.protocol.message.PingKademliaMessage;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.decentralized.protocol.message.RepublishMessage;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.service.DistributionJobManager;
import io.ep2p.somnia.util.ExternalNodeHelper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.math.BigInteger;
import java.util.Optional;


@Slf4j
public class RepublishRequestMessageHandler implements MessageHandler<BigInteger, SomniaConnectionInfo> {
    private final KeyHashGenerator<BigInteger, SomniaKey> somniaKeyHashGenerator;
    private final DistributionJobManager distributionJobManager;

    public RepublishRequestMessageHandler(KeyHashGenerator<BigInteger, SomniaKey> somniaKeyHashGenerator, DistributionJobManager distributionJobManager) {
        this.somniaKeyHashGenerator = somniaKeyHashGenerator;
        this.distributionJobManager = distributionJobManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<BigInteger, SomniaConnectionInfo, ?>, O extends KademliaMessage<BigInteger, SomniaConnectionInfo, ?>> O handle(KademliaNodeAPI<BigInteger, SomniaConnectionInfo> kademliaNode, I message) {
        assert message instanceof RepublishMessage;
        var republishMessage = (RepublishMessage) message;
        republishMessage.getData().setTries(republishMessage.getData().getTries() + 1);

        var hash = somniaKeyHashGenerator.generateHash(republishMessage.getData().getKey());
        var findNodeAnswer = kademliaNode.getRoutingTable().findClosest(hash);

        for (ExternalNode<BigInteger, SomniaConnectionInfo> externalNode: findNodeAnswer.getNodes()) {
            if (externalNode.getId().equals(kademliaNode.getId())){
                distributionJobManager.addJob(republishMessage.getData().getKey(), republishMessage.getData().getRequester(), kademliaNode);
                kademliaNode.getMessageSender().sendAsyncMessage(
                        kademliaNode,
                        externalNode,
                        new PingKademliaMessage<>()
                );
                break;
            }

            try {
                Optional<Boolean> optional = ExternalNodeHelper.performIfAlive(kademliaNode, externalNode, () -> {
                    kademliaNode.getMessageSender().sendAsyncMessage(
                            kademliaNode,
                            externalNode,
                            republishMessage
                    );
                    return true;
                });
                if (optional.isPresent()){
                    break;
                }
            } catch (Exception e) {
                log.error("Failed to forward RepublishMessage to node " + externalNode.getId(), e);
            }
        }

        return (O) new EmptyKademliaMessage();
    }

}
