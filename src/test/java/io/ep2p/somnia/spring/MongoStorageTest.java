package io.ep2p.somnia.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import io.ep2p.somnia.config.SomniaAutoConfiguration;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;
import io.ep2p.somnia.model.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.spring.configuration.LocalNodeConnectionApi;
import io.ep2p.somnia.spring.configuration.SomniaTestConfiguration;
import io.ep2p.somnia.spring.mock.SampleData;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity2;
import io.ep2p.somnia.storage.MongoStorage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigInteger;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SomniaTestConfiguration.class, SomniaAutoConfiguration.class})
@ActiveProfiles({"default", "test"})
@Slf4j
public class MongoStorageTest {
    //todo: add tests to check data uniqueness and meta query

    @Autowired
    MongoTemplate mongoTemplate;
    MongoStorage mongoStorage = null;
    ObjectMapper objectMapper = null;

    @BeforeEach
    public void setup(@Autowired MongoTemplate mongoTemplate, @Autowired ObjectMapper objectMapper,
                      @Autowired NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
                      @Autowired SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode){
        log.info("Setting up the test");
        log.info("Dropping all collection");
        mongoTemplate.getCollectionNames().forEach(collectionName -> {
            mongoTemplate.remove(new Query(), collectionName);
        });
        ((LocalNodeConnectionApi<BigInteger>) nodeConnectionApi).registerNode(somniaKademliaSyncRepositoryNode);
        this.mongoStorage = new MongoStorage(mongoTemplate, objectMapper);
        this.objectMapper = objectMapper;
    }

    @Test
    public void store() {
        SampleData sampleData = SampleData.builder()
                .integerVal(1)
                .stringVal("hey")
                .build();

        JsonNode jsonNode = objectMapper.valueToTree(sampleData);

        log.info("Inserting: " + jsonNode.toString());

        mongoStorage.store(SampleSomniaEntity.class, true, SomniaKey.builder()
                .key(BigInteger.valueOf(2000))
                .build(), SomniaValue.builder()
                .data(jsonNode)
                .build());

        log.info("Inserted data without an error");
    }

    @Test
    public void get() throws JsonProcessingException {
        SomniaKey somniaKey = SomniaKey.builder()
                .key(BigInteger.valueOf(3000))
                .build();

        SampleData sampleData = SampleData.builder()
                .integerVal(1)
                .stringVal("hey")
                .build();
        JsonNode jsonNode = objectMapper.valueToTree(sampleData);
        mongoStorage.store(SampleSomniaEntity.class, true, somniaKey, SomniaValue.builder()
                .data(jsonNode)
                .build());

        log.info("Getting data from database");
        SomniaValue somniaValue = mongoStorage.get(SampleSomniaEntity.class, somniaKey);

        Assertions.assertTrue(somniaValue.isExists());
        Assertions.assertEquals(1, somniaValue.getCount());
        log.info("SomniaValue fields seem correct, checking data");

        List<SampleData> sampleDataList = this.objectMapper.readValue(
                somniaValue.getData().toString(), new TypeReference<List<SampleData>>(){}
        );
        Assertions.assertEquals(1, sampleDataList.size());
        Assertions.assertEquals(sampleDataList.get(0), sampleData);

        log.info("SomniaValue data is correct.");
    }

    @Test
    public void contains(){
        SomniaKey somniaKey = SomniaKey.builder()
                .key(BigInteger.valueOf(3000))
                .build();

        SampleData sampleData = SampleData.builder()
                .integerVal(1)
                .stringVal("hey")
                .build();
        JsonNode jsonNode = objectMapper.valueToTree(sampleData);
        mongoStorage.store(SampleSomniaEntity.class, true, somniaKey, SomniaValue.builder()
                .data(jsonNode)
                .build());

        boolean contains = mongoStorage.contains(SampleSomniaEntity.class, somniaKey);
        Assertions.assertTrue(contains);
        log.info("Passed contains() test");
    }

    @Test
    public void testUniqueStore(){
        SampleData sampleData = SampleData.builder()
                .integerVal(1)
                .stringVal("hey")
                .build();

        JsonNode jsonNode = objectMapper.valueToTree(sampleData);

        SomniaKey somniaKey = SomniaKey.builder()
                .key(BigInteger.valueOf(4000))
                .build();

        mongoStorage.store(SampleSomniaEntity.class, true, somniaKey,
                SomniaValue.builder()
                        .data(jsonNode)
                        .build());

        mongoStorage.store(SampleSomniaEntity.class, true, somniaKey,
                SomniaValue.builder()
                        .data(jsonNode)
                        .build());

        SomniaValue somniaValue = mongoStorage.get(SampleSomniaEntity.class, somniaKey);
        Assertions.assertEquals(1, somniaValue.getCount());
        Assertions.assertEquals(1, somniaValue.getData().size());
    }

    @Test
    public void testNoneUniqueStore(){
        SampleData sampleData = SampleData.builder()
                .integerVal(1)
                .stringVal("hey")
                .build();

        JsonNode jsonNode = objectMapper.valueToTree(sampleData);

        SomniaKey somniaKey = SomniaKey.builder()
                .key(BigInteger.valueOf(4000))
                .build();

        mongoStorage.store(SampleSomniaEntity2.class, true, somniaKey,
                SomniaValue.builder()
                        .data(jsonNode)
                        .build());

        mongoStorage.store(SampleSomniaEntity2.class, true, somniaKey,
                SomniaValue.builder()
                        .data(jsonNode)
                        .build());

        SomniaValue somniaValue = mongoStorage.get(SampleSomniaEntity2.class, somniaKey);
        Assertions.assertEquals(2, somniaValue.getCount());
        Assertions.assertEquals(2, somniaValue.getData().size());
    }

    @Test
    public void testQuery(){
        SomniaKey somniaKey = SomniaKey.builder()
                .key(BigInteger.valueOf(5000))
                .build();

        SampleData sampleData = SampleData.builder()
                .integerVal(1)
                .stringVal("valid")
                .build();
        JsonNode jsonNode = objectMapper.valueToTree(sampleData);

        mongoStorage.store(SampleSomniaEntity.class, true, somniaKey, SomniaValue.builder()
                .data(jsonNode)
                .build());

        boolean contains = mongoStorage.contains(SampleSomniaEntity.class, somniaKey);
        Assertions.assertTrue(contains);

        somniaKey.getMeta().setQuery("{\"data.stringVal\": \"invalid\"}");
        SomniaValue somniaValue = mongoStorage.get(SampleSomniaEntity.class, somniaKey);
        Assertions.assertEquals(0, somniaValue.getCount());
        Assertions.assertEquals(0, somniaValue.getData().size());

        somniaKey.getMeta().setQuery("{\"data.stringVal\": \"valid\"}");
        somniaValue = mongoStorage.get(SampleSomniaEntity.class, somniaKey);
        Assertions.assertEquals(1, somniaValue.getCount());
        Assertions.assertEquals(1, somniaValue.getData().size());
    }

}
