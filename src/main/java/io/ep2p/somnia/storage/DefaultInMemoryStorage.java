package io.ep2p.somnia.storage;

import io.ep2p.somnia.model.SomniaEntity;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultInMemoryStorage implements Storage {
    private final Map<BigInteger, SomniaValue> map = new ConcurrentHashMap<>();

    @Override
    public void store(Class<? extends SomniaEntity<?>> classOfName, boolean uniqueKey, SomniaKey somniaKey, SomniaValue somniaValue) {
        this.map.putIfAbsent(somniaKey.getKey(), somniaValue);
    }

    @Override
    public SomniaValue get(Class<? extends SomniaEntity<?>> classOfName, SomniaKey somniaKey) {
        SomniaValue somniaValue = this.map.get(somniaKey.getKey());

        if (somniaValue != null)
            return somniaValue;

        return SomniaValue.builder()
                .exists(false)
                .build();
    }

    @Override
    public boolean contains(Class<? extends SomniaEntity<?>> classOfName, SomniaKey somniaKey) {
        return this.map.containsKey(somniaKey.getKey());
    }
}
