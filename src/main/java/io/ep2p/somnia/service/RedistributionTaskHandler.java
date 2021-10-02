package io.ep2p.somnia.service;

import io.ep2p.kademlia.node.KademliaRepository;
import io.ep2p.kademlia.node.Node;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;

import java.math.BigInteger;

public interface RedistributionTaskHandler extends Runnable {
    void init(KademliaRepository<SomniaKey, SomniaValue> kademliaRepository);
    void addTask(SomniaKey somniaKey, Node<BigInteger, SomniaConnectionInfo> publisher);
}
