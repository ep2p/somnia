package io.ep2p.somnia.service;

import java.io.Serializable;
import java.math.BigInteger;

public interface HashGenerator {
    <E extends Serializable> BigInteger hash(BigInteger id, E obj);

    class DefaultHashGenerator implements HashGenerator {
        @Override
        public <E extends Serializable> BigInteger hash(BigInteger id, E obj) {
            return id;
        }
    }
}
