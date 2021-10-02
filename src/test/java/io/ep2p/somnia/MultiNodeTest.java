package io.ep2p.somnia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.exception.BootstrapException;
import io.ep2p.kademlia.exception.GetException;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.somnia.decentralized.*;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.spring.configuration.LocalNodeConnectionApi;
import io.ep2p.somnia.spring.mock.EmptyDistributionTaskHandler;
import io.ep2p.somnia.spring.mock.SampleData;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity2;
import io.ep2p.somnia.storage.DefaultInMemoryStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

public class MultiNodeTest {
    private SomniaKademliaSyncRepositoryNode node1;
    private SomniaKademliaSyncRepositoryNode node2;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    public void setUp(){
        BigInteger node1Id = BigInteger.valueOf(1L);
        BigInteger node2Id = BigInteger.valueOf(2L);

        SomniaEntityManager somniaEntityManager = new DefaultSomniaEntityManager();
        somniaEntityManager.register(SampleSomniaEntity.class);
        somniaEntityManager.register(SampleSomniaEntity2.class);
        LocalNodeConnectionApi<BigInteger> localNodeConnectionApi = new LocalNodeConnectionApi<>();

        SomniaKademliaRepository somniaKademliaRepository1 = new SomniaKademliaRepository(somniaEntityManager, new DefaultInMemoryStorage(objectMapper), new DefaultInMemoryStorage(objectMapper));
        SomniaKademliaRepository somniaKademliaRepository2 = new SomniaKademliaRepository(somniaEntityManager, new DefaultInMemoryStorage(objectMapper), new DefaultInMemoryStorage(objectMapper));


        this.node1 = new SomniaKademliaSyncRepositoryNode(node1Id, localNodeConnectionApi, new SomniaConnectionInfo(), NodeSettings.Default.build(), somniaKademliaRepository1, somniaEntityManager, new EmptyDistributionTaskHandler());
        this.node2 = new SomniaKademliaSyncRepositoryNode(node2Id, localNodeConnectionApi, new SomniaConnectionInfo(), NodeSettings.Default.build(), somniaKademliaRepository2, somniaEntityManager, new EmptyDistributionTaskHandler());
        this.node1.start();
        this.node2.start();
        localNodeConnectionApi.registerNode(node1);
        localNodeConnectionApi.registerNode(node2);
    }

    @Test
    public void test_hit() throws BootstrapException, StoreException, GetException, JsonProcessingException {
        this.node1.bootstrap(this.node2);

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
                        .build(),
                true
        );

        GetAnswer<BigInteger, SomniaKey, SomniaValue> getAnswer = this.node2.get(SomniaKey.builder()
                .hash(BigInteger.valueOf(2))
                .key(BigInteger.valueOf(2))
                .name(SampleSomniaEntity.class.getName())
                .build());

        Assertions.assertEquals(getAnswer.getResult(), GetAnswer.Result.FOUND);
        Assertions.assertEquals(getAnswer.getValue().getCount(), 1);
        List<SampleData> sampleDataList = this.objectMapper.readValue(
                getAnswer.getValue().getData().toString(), new TypeReference<List<SampleData>>(){}
        );
        Assertions.assertEquals(sampleDataList.size(), 1);
        Assertions.assertEquals(sampleDataList.get(0).getIntegerVal(), 2);
        Assertions.assertEquals(sampleDataList.get(0).getStringVal(), "store on node 2");
    }

    @Test
    public void test_distribute() throws BootstrapException, StoreException, GetException, JsonProcessingException {
        this.node1.bootstrap(this.node2);

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
                        .build(),
                true
        );

        GetAnswer<BigInteger, SomniaKey, SomniaValue> getAnswer = this.node2.get(SomniaKey.builder()
                .hash(BigInteger.valueOf(2))
                .key(BigInteger.valueOf(2))
                .name(SampleSomniaEntity.class.getName())
                .build());

        Assertions.assertEquals(getAnswer.getResult(), GetAnswer.Result.FOUND);
        Assertions.assertEquals(getAnswer.getValue().getCount(), 1);
        List<SampleData> sampleDataList = this.objectMapper.readValue(
                getAnswer.getValue().getData().toString(), new TypeReference<List<SampleData>>(){}
        );
        Assertions.assertEquals(sampleDataList.size(), 1);
        Assertions.assertEquals(sampleDataList.get(0).getIntegerVal(), 2);
        Assertions.assertEquals(sampleDataList.get(0).getStringVal(), "store on all");


        getAnswer = this.node1.get(SomniaKey.builder()
                .hash(BigInteger.valueOf(2))
                .key(BigInteger.valueOf(2))
                .name(SampleSomniaEntity.class.getName())
                .build());

        Assertions.assertEquals(getAnswer.getResult(), GetAnswer.Result.FOUND);
        Assertions.assertEquals(getAnswer.getValue().getCount(), 1);
        sampleDataList = this.objectMapper.readValue(
                getAnswer.getValue().getData().toString(), new TypeReference<List<SampleData>>(){}
        );
        Assertions.assertEquals(sampleDataList.size(), 1);
        Assertions.assertEquals(sampleDataList.get(0).getIntegerVal(), 2);
        Assertions.assertEquals(sampleDataList.get(0).getStringVal(), "store on all");
    }

}
