package io.ep2p.somnia.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.somnia.model.SomniaEntity;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCacheStorage implements Storage {
    private final Map<BigInteger, SomniaValue> map = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public DefaultCacheStorage(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DefaultCacheStorage(){
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void store(Class<? extends SomniaEntity> classOfName, boolean uniqueKey, SomniaKey somniaKey, SomniaValue somniaValue) {
        this.map.putIfAbsent(somniaKey.getKey(), somniaValue);
    }

    @Override
    public SomniaValue get(Class<? extends SomniaEntity> classOfName, SomniaKey somniaKey) {
        SomniaValue somniaValue = this.map.get(somniaKey.getKey());
        List<JsonNode> values = new ArrayList<>();

        if (somniaValue != null)
            values.add(somniaValue.getData());

        JsonNode data = objectMapper.valueToTree(values);
        return SomniaValue.builder()
                .data(data)
                .count(values.size())
                .exists(values.size() > 0)
                .build();
    }

    @Override
    public boolean contains(Class<? extends SomniaEntity> classOfName, SomniaKey somniaKey) {
        return this.map.containsKey(somniaKey.getKey());
    }
}
