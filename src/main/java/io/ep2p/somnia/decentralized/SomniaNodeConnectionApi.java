package io.ep2p.somnia.decentralized;

import io.ep2p.kademlia.connection.NodeConnectionApi;
import io.ep2p.kademlia.node.Node;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;

import java.math.BigInteger;
import java.util.List;

public interface SomniaNodeConnectionApi extends NodeConnectionApi<BigInteger, SomniaConnectionInfo> {
    List<SomniaValue> readForKey(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> node, SomniaKey somniaKey, int page, int limit);
    boolean deleteForKey(Node<BigInteger, SomniaConnectionInfo> caller, Node<BigInteger, SomniaConnectionInfo> node, SomniaKey somniaKey);
}
