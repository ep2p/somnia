package io.ep2p.somnia.service;

import io.ep2p.kademlia.node.KademliaRepository;
import io.ep2p.kademlia.node.Node;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;

import java.math.BigInteger;

public class DefaultRedistributionTaskHandler implements RedistributionTaskHandler {
    private KademliaRepository<SomniaKey, SomniaValue> kademliaRepository;

    @Override
    public synchronized void init(KademliaRepository<SomniaKey, SomniaValue> kademliaRepository) {
        assert kademliaRepository != null;
        if (this.kademliaRepository == null){
            this.kademliaRepository = kademliaRepository;
            this.start();
        }
    }

    @Override
    public void addTask(SomniaKey somniaKey, Node<BigInteger, SomniaConnectionInfo> publisher) {

    }

    private void start(){

    }

    @Override
    public void run() {

    }
}
