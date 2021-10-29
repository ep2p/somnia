package io.ep2p.somnia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.table.BigIntegerRoutingTable;
import io.ep2p.somnia.decentralized.*;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.spring.configuration.TestMessageSenderAPI;
import io.ep2p.somnia.spring.mock.SampleData;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity2;
import io.ep2p.somnia.storage.DefaultCacheStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MultiNodeTest {
    private SomniaDHTKademliaNode node1;
    private SomniaDHTKademliaNode node2;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    public void setUp(){
        BigInteger node1Id = BigInteger.valueOf(1L);
        BigInteger node2Id = BigInteger.valueOf(2L);

        SomniaEntityManager somniaEntityManager = new DefaultSomniaEntityManager();
        somniaEntityManager.register(SampleSomniaEntity.class);
        somniaEntityManager.register(SampleSomniaEntity2.class);
        TestMessageSenderAPI<BigInteger, SomniaConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        SomniaKademliaRepository somniaKademliaRepository1 = new SomniaKademliaRepository(somniaEntityManager, new DefaultCacheStorage(objectMapper), new DefaultCacheStorage(objectMapper));
        SomniaKademliaRepository somniaKademliaRepository2 = new SomniaKademliaRepository(somniaEntityManager, new DefaultCacheStorage(objectMapper), new DefaultCacheStorage(objectMapper));
        NodeSettings nodeSettings = NodeSettings.Default.build();

        KeyHashGenerator<BigInteger, SomniaKey> keyHashGenerator = new KeyHashGenerator<BigInteger, SomniaKey>() {
            @Override
            public BigInteger generateHash(SomniaKey key) {
                return key.getKey();
            }
        };
        SomniaStorageConfig somniaStorageConfig = new SomniaStorageConfig();


        this.node1 = new SomniaDHTKademliaNode(node1Id, new SomniaConnectionInfo(), new BigIntegerRoutingTable<>(node1Id, nodeSettings), messageSenderAPI, nodeSettings, somniaKademliaRepository1, keyHashGenerator, somniaEntityManager, somniaStorageConfig);
        this.node2 = new SomniaDHTKademliaNode(node2Id, new SomniaConnectionInfo(), new BigIntegerRoutingTable<>(node1Id, nodeSettings), messageSenderAPI, nodeSettings, somniaKademliaRepository2, keyHashGenerator, somniaEntityManager, somniaStorageConfig);
        this.node2.start();
        messageSenderAPI.registerNode(node1);
        messageSenderAPI.registerNode(node2);
    }

    @Test
    public void test_hit() throws JsonProcessingException, ExecutionException, InterruptedException {
        this.node1.start(this.node2);

        this.node1.store(
                SomniaKey.builder()
                        .hash(BigInteger.valueOf(2))
                        .key(BigInteger.valueOf(2))
                        .name(SampleSomniaEntity.class.getName())
                        .build(),
                SomniaValue.builder()
                        .data(objectMapper.valueToTree(SampleData.builder()
                                .integerVal(2)
                                .stringVal("store on node 2")
                                .build()))
                        .build()
        );

        LookupAnswer<BigInteger, SomniaKey, SomniaValue> lookupAnswer = this.node2.lookup(SomniaKey.builder()
                .hash(BigInteger.valueOf(2))
                .key(BigInteger.valueOf(2))
                .name(SampleSomniaEntity.class.getName())
                .build()).get();

        Assertions.assertEquals(lookupAnswer.getResult(), LookupAnswer.Result.FOUND);
        Assertions.assertEquals(lookupAnswer.getValue().getCount(), 1);
        List<SampleData> sampleDataList = this.objectMapper.readValue(
                lookupAnswer.getValue().getData().toString(), new TypeReference<List<SampleData>>(){}
        );
        Assertions.assertEquals(sampleDataList.size(), 1);
        Assertions.assertEquals(sampleDataList.get(0).getIntegerVal(), 2);
        Assertions.assertEquals(sampleDataList.get(0).getStringVal(), "store on node 2");
    }

    @Test
    public void test_distribute() throws JsonProcessingException, ExecutionException, InterruptedException {
        this.node1.start(this.node2);

        this.node1.store(
                SomniaKey.builder()
                        .hash(BigInteger.valueOf(2))
                        .key(BigInteger.valueOf(2))
                        .name(SampleSomniaEntity2.class.getName())
                        .build(),
                SomniaValue.builder()
                        .data(objectMapper.valueToTree(SampleData.builder()
                                .integerVal(2)
                                .stringVal("store on all")
                                .build()))
                        .build()
        );

        LookupAnswer<BigInteger, SomniaKey, SomniaValue> lookupAnswer = this.node2.lookup(SomniaKey.builder()
                .hash(BigInteger.valueOf(2))
                .key(BigInteger.valueOf(2))
                .name(SampleSomniaEntity.class.getName())
                .build()).get();

        Assertions.assertEquals(lookupAnswer.getResult(), LookupAnswer.Result.FOUND);
        Assertions.assertEquals(lookupAnswer.getValue().getCount(), 1);
        List<SampleData> sampleDataList = this.objectMapper.readValue(
                lookupAnswer.getValue().getData().toString(), new TypeReference<List<SampleData>>(){}
        );
        Assertions.assertEquals(sampleDataList.size(), 1);
        Assertions.assertEquals(sampleDataList.get(0).getIntegerVal(), 2);
        Assertions.assertEquals(sampleDataList.get(0).getStringVal(), "store on all");


        lookupAnswer = this.node1.lookup(SomniaKey.builder()
                .hash(BigInteger.valueOf(2))
                .key(BigInteger.valueOf(2))
                .name(SampleSomniaEntity.class.getName())
                .build()).get();

        Assertions.assertEquals(lookupAnswer.getResult(), LookupAnswer.Result.FOUND);
        Assertions.assertEquals(lookupAnswer.getValue().getCount(), 1);
        sampleDataList = this.objectMapper.readValue(
                lookupAnswer.getValue().getData().toString(), new TypeReference<List<SampleData>>(){}
        );
        Assertions.assertEquals(sampleDataList.size(), 1);
        Assertions.assertEquals(sampleDataList.get(0).getIntegerVal(), 2);
        Assertions.assertEquals(sampleDataList.get(0).getStringVal(), "store on all");
    }

}
