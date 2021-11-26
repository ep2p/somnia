package io.ep2p.somnia.decentralized.protocol.handler;

import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.node.KademliaNodeAPI;
import io.ep2p.kademlia.protocol.handler.MessageHandler;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.decentralized.SomniaKademliaRepository;
import io.ep2p.somnia.decentralized.protocol.message.ChunkRequestMessage;
import io.ep2p.somnia.decentralized.protocol.message.ChunkResponseMessage;
import io.ep2p.somnia.decentralized.protocol.message.RepublishMessage;
import io.ep2p.somnia.model.SomniaValue;
import lombok.var;

import java.math.BigInteger;


public class ChunkRequestMessageHandler implements MessageHandler<BigInteger, SomniaConnectionInfo> {
    private final SomniaKademliaRepository somniaKademliaRepository;

    public ChunkRequestMessageHandler(SomniaKademliaRepository somniaKademliaRepository) {
        this.somniaKademliaRepository = somniaKademliaRepository;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends KademliaMessage<BigInteger, SomniaConnectionInfo, ?>, O extends KademliaMessage<BigInteger, SomniaConnectionInfo, ?>> O handle(KademliaNodeAPI<BigInteger, SomniaConnectionInfo> kademliaNode, I message) {
        assert message instanceof ChunkRequestMessage;
        var chunkRequestMessage = (RepublishMessage) message;
        try {
            kademliaNode.getRoutingTable().update(chunkRequestMessage.getNode());
        } catch (FullBucketException ignored) {}

        SomniaValue somniaValue = this.somniaKademliaRepository.get(chunkRequestMessage.getData().getKey());
        ChunkResponseMessage chunkResponseMessage = new ChunkResponseMessage();
        chunkResponseMessage.setNode(kademliaNode);
        chunkResponseMessage.setData(ChunkResponseMessage.ChunkResponseData.builder()
                .key(chunkRequestMessage.getData().getKey())
                .value(somniaValue)
                .build());
        return (O) chunkRequestMessage;
    }
}
