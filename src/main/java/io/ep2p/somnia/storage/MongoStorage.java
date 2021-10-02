package io.ep2p.somnia.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.somnia.model.SomniaEntity;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.service.HashGenerator;
import io.ep2p.somnia.util.QueryUtil;
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
    private final HashGenerator hashGenerator;

    public MongoStorage(MongoTemplate mongoTemplate, ObjectMapper objectMapper, HashGenerator hashGenerator) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
        this.hashGenerator = hashGenerator;
    }

    @SneakyThrows
    @Override
    public void store(Class<? extends SomniaEntity> classOfName, boolean uniqueKey, SomniaKey somniaKey, SomniaValue somniaValue) {
        if (somniaValue.getCount() > 0){
            throw new IllegalArgumentException("Default mongo storage implementation is incapable of storing list of values");
        }
        SomniaEntity somniaEntity = classOfName.newInstance();
        Object o = objectMapper.readValue(somniaValue.getData().toString(), somniaEntity.getGenericClassType(0));
        somniaEntity.setData((Serializable) o);
        somniaEntity.setValueHash(hashGenerator.hashSomniaValueData(somniaValue.getData().toString()));
        somniaEntity.setKey(somniaKey.getKeyAsString());
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
    public SomniaValue get(Class<? extends SomniaEntity> classOfName, SomniaKey somniaKey) {
        Query baseQuery = QueryUtil.generateQuery(somniaKey);

        long count = mongoTemplate.count(baseQuery, classOfName);

        baseQuery.skip(somniaKey.getMeta().getOffset());
        baseQuery.limit(somniaKey.getMeta().getLimit());

        List<? extends SomniaEntity> result = mongoTemplate.find(baseQuery, classOfName);

        List<Object> values = new ArrayList<>();
        result.forEach(somniaEntity -> {
            values.add(somniaEntity.getData());
        });

        JsonNode jsonNode = objectMapper.valueToTree(values);
        return SomniaValue.builder()
                .count(count)
                .data(jsonNode)
                .exists(true)
                .build();
    }

    @Override
    public boolean contains(Class<? extends SomniaEntity> classOfName, SomniaKey somniaKey) {
        return mongoTemplate.exists(Query.query(Criteria.where("key").is(somniaKey.getKeyAsString())), classOfName);
    }
}
