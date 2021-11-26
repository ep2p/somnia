package io.ep2p.somnia.decentralized.protocol.message;

import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

import static io.ep2p.somnia.decentralized.protocol.SomniaMessageType.REPUBLISH_CHUNK_RESPONSE;

public class ChunkResponseMessage extends KademliaMessage<BigInteger, SomniaConnectionInfo, ChunkResponseMessage.ChunkResponseData> {

    public ChunkResponseMessage() {
        super(REPUBLISH_CHUNK_RESPONSE);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChunkResponseData implements Serializable {
        private SomniaKey key;
        private SomniaValue value;
    }
}
