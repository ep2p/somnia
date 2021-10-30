package io.ep2p.somnia.decentralized;

import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.somnia.model.SomniaKey;

import java.math.BigInteger;

public class SomniaKeyHashGenerator implements KeyHashGenerator<BigInteger, SomniaKey> {

    @Override
    public BigInteger generateHash(SomniaKey somniaKey) {
        return somniaKey.getHitNode() != null ? somniaKey.getHitNode() : somniaKey.getHash();
    }
}
