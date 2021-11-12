package io.ep2p.somnia.decentralized.protocol.message;

import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.protocol.message.KademliaMessage;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

import static io.ep2p.somnia.decentralized.protocol.SomniaMessageType.REPUBLISH_REQUEST;

public class RepublishMessage extends KademliaMessage<BigInteger, SomniaConnectionInfo, RepublishMessage.RepublishData> {

    protected RepublishMessage() {
        super(REPUBLISH_REQUEST);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RepublishData implements Serializable {
        private Node<BigInteger, SomniaConnectionInfo> requester;
        private SomniaKey somniaKey;
        private int tries;
    }
}
