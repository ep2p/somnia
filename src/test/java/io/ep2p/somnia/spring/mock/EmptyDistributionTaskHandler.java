package io.ep2p.somnia.spring.mock;

import io.ep2p.kademlia.node.Node;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.service.RedistributionTaskHandler;

import java.math.BigInteger;

public class EmptyDistributionTaskHandler implements RedistributionTaskHandler {
    @Override
    public void init(SomniaKademliaSyncRepositoryNode selfNode) {

    }

    @Override
    public void addTask(SomniaKey somniaKey, Node<BigInteger, SomniaConnectionInfo> publisher) {

    }

    @Override
    public void run() {

    }
}