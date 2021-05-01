package io.ep2p.somnia.storage;

import com.fasterxml.jackson.databind.JsonNode;
import io.ep2p.somnia.model.SomniaValue;

import java.math.BigInteger;

public interface InMemoryStorage {
    void store(BigInteger key, JsonNode data);
    SomniaValue get(BigInteger key);
    boolean contains(BigInteger key);
}
