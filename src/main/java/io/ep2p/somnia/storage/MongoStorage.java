package io.ep2p.somnia.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.somnia.model.SomniaEntity;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import lombok.SneakyThrows;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoStorage implements Storage {
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public MongoStorage(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    @Override
    public void store(Class<? extends SomniaEntity<?>> classOfName, boolean uniqueKey, SomniaKey somniaKey, SomniaValue somniaValue) {
        SomniaEntity somniaEntity = classOfName.newInstance();
        Object o = objectMapper.readValue(somniaValue.toString(), somniaEntity.getListenerMessageBodyClassType(0));
        somniaEntity.setData((Serializable) o);
        somniaEntity.setKey(somniaKey.getKey());
        somniaEntity.setCreationDate(new Date());
        if (uniqueKey){
            try {
                mongoTemplate.save(somniaEntity);
            }catch (DuplicateKeyException | com.mongodb.DuplicateKeyException ignored){}
            return;
        }
        mongoTemplate.save(somniaEntity);
    }

    @Override
    public SomniaValue get(Class<? extends SomniaEntity<?>> classOfName, SomniaKey somniaKey) {
        List<? extends SomniaEntity<?>> result = mongoTemplate.find(Query.query(Criteria.where("key").is(somniaKey.getKey())), classOfName);

        List<Object> values = new ArrayList<>();
        result.forEach(somniaEntity -> {
            values.add(somniaEntity.getData());
        });

        JsonNode jsonNode = objectMapper.valueToTree(values);
        return SomniaValue.builder()
                .data(jsonNode)
                .exists(true)
                .build();
    }

    @Override
    public boolean contains(Class<? extends SomniaEntity<?>> classOfName, SomniaKey somniaKey) {
        return mongoTemplate.exists(Query.query(Criteria.where("key").is(somniaKey.getKey())), classOfName);
    }
}
