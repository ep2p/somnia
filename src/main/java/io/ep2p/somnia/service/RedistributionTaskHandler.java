package io.ep2p.somnia.service;

import io.ep2p.kademlia.node.Node;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;
import io.ep2p.somnia.model.SomniaKey;

import java.math.BigInteger;

public interface RedistributionTaskHandler extends Runnable {
    void init(SomniaKademliaSyncRepositoryNode selfNode);
    void addTask(SomniaKey somniaKey, Node<BigInteger, SomniaConnectionInfo> publisher);
}
