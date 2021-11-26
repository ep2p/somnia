package io.ep2p.somnia.decentralized.protocol.message;

import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

import static io.ep2p.somnia.decentralized.protocol.SomniaMessageType.REPUBLISH_CHUNK_REQUEST;

public class ChunkRequestMessage extends KademliaMessage<BigInteger, SomniaConnectionInfo, ChunkRequestMessage.ChunkRequestData> {

    public ChunkRequestMessage() {
        super(REPUBLISH_CHUNK_REQUEST);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChunkRequestData implements Serializable {
        private SomniaKey somniaKey;
    }
}
