package io.ep2p.somnia.service;

import io.ep2p.kademlia.node.Node;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;

import java.math.BigInteger;
import java.util.List;

public interface DistributionJobManager {
    void addJob(SomniaKey somniaKey, Node<BigInteger, SomniaConnectionInfo> node);
    void removeJob(SomniaKey somniaKey);
    List<SomniaKey> getJobs();
    void start();
}
