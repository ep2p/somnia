package io.ep2p.somnia.storage;

import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;

public class MongoStorage implements Storage {

    @Override
    public void store(Class<?> classOfName, boolean uniqueKey, SomniaKey somniaKey, SomniaValue somniaValue) {

    }

    @Override
    public SomniaValue get(Class<?> classOfName, SomniaKey somniaKey) {
        return null;
    }

    @Override
    public boolean contains(Class<?> classOfName, SomniaKey somniaKey) {
        return false;
    }
}
