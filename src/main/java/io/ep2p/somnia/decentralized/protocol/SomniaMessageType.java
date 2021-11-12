package io.ep2p.somnia.decentralized.protocol;

import io.ep2p.kademlia.protocol.MessageType;

public interface SomniaMessageType extends MessageType {
    String REPUBLISH_REQUEST = "REPUBLISH_REQUEST";
    String REPUBLISH_CHUNK_REQUEST = "REPUBLISH_CHUNK_REQUEST";
    String REPUBLISH_CHUNK_RESPONSE = "REPUBLISH_CHUNK_RESPONSE";
}
