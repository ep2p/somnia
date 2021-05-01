package io.ep2p.somnia.storage;

import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;

public interface MongoStorage {
    void store(Class<?> classOfName, boolean uniqueKey, SomniaKey somniaKey, SomniaValue somniaValue);
    SomniaValue get(Class<?> classOfName, SomniaKey somniaKey);
    boolean contains(Class<?> classOfName, SomniaKey somniaKey);
}
