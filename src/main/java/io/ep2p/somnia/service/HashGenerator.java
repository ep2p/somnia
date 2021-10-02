package io.ep2p.somnia.service;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static com.google.common.hash.Hashing.sha256;

public interface HashGenerator {
    <E extends Serializable> BigInteger hash(BigInteger id, E obj);
    long hashSomniaValueData(String json);

    class DefaultHashGenerator implements HashGenerator {
        @Override
        public <E extends Serializable> BigInteger hash(BigInteger id, E obj) {
            return id;
        }

        @Override
        public long hashSomniaValueData(String json) {
            //noinspection UnstableApiUsage
            return sha256().hashString(json, StandardCharsets.UTF_8).asLong();
        }
    }
}
