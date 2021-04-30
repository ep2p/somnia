package io.ep2p.somnia.decentralized;

import com.github.ep2p.kademlia.util.KeyHashGenerator;
import io.ep2p.somnia.model.SomniaKey;

import java.math.BigInteger;

public class SomniaKeyHashGenerator implements KeyHashGenerator<BigInteger, SomniaKey> {

    @Override
    public BigInteger generate(SomniaKey somniaKey) {
        return somniaKey.getHitNode() != null ? somniaKey.getHitNode() : somniaKey.getHash();
    }
}
