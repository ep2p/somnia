package io.ep2p.somnia;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.exception.DuplicateStoreRequest;
import io.ep2p.kademlia.exception.FullBucketException;
import io.ep2p.kademlia.model.LookupAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.table.BigIntegerRoutingTable;
import io.ep2p.somnia.decentralized.*;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.spring.configuration.TestMessageSenderAPI;
import io.ep2p.somnia.spring.mock.SampleData;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity3;
import io.ep2p.somnia.storage.DefaultCacheStorage;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class DistributionTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<SomniaDHTKademliaNode> nodes = new ArrayList<>();
    private final int nodeSize = 100;
    private final long timeout = 32000;

    private void init_network(SomniaStorageConfig somniaStorageConfig) throws ExecutionException, InterruptedException {
        nodes.clear();
        NodeSettings.Default.PING_SCHEDULE_TIME_VALUE = 5;
        NodeSettings.Default.PING_SCHEDULE_TIME_UNIT = TimeUnit.SECONDS;
        NodeSettings.Default.BUCKET_SIZE = 256;
        NodeSettings.Default.IDENTIFIER_SIZE = 256;
        NodeSettings.Default.ALPHA = 30;


        SomniaEntityManager somniaEntityManager = new DefaultSomniaEntityManager();
        somniaEntityManager.register(SampleSomniaEntity3.class);
        TestMessageSenderAPI<BigInteger, SomniaConnectionInfo> messageSenderAPI = new TestMessageSenderAPI<>();

        KeyHashGenerator<BigInteger, SomniaKey> keyHashGenerator = new KeyHashGenerator<BigInteger, SomniaKey>() {
            @Override
            public BigInteger generateHash(SomniaKey key) {
                return key.getKey();
            }
        };

        NodeSettings nodeSettings = NodeSettings.Default.build();

        SomniaDHTKademliaNode previousNode = null;
        for(int i = 0; i < this.nodeSize; i++){
            SomniaKademliaRepository somniaKademliaRepository = new SomniaKademliaRepository(somniaEntityManager, new DefaultCacheStorage(objectMapper), new DefaultCacheStorage(objectMapper));
            var node = new SomniaDHTKademliaNode(BigInteger.valueOf(i), new SomniaConnectionInfo(), new BigIntegerRoutingTable<>(BigInteger.valueOf(i), nodeSettings), messageSenderAPI, nodeSettings, somniaKademliaRepository, keyHashGenerator, somniaEntityManager, somniaStorageConfig);
            messageSenderAPI.registerNode(node);
            if (previousNode != null)
                node.start(previousNode).get();
            else
                node.start();
            nodes.add(node);
            previousNode = node;
        }
    }


    /*
    *  Proof that Somnia can distribute data for at least 1/4 of the network
    */
    @Test
    public void minimumDistributionTest() throws InterruptedException, ExecutionException, DuplicateStoreRequest {
        init_network(SomniaStorageConfig.builder()
                .forceStore(true)
                .perNodeDistribution(20)
                .minimumDistribution(this.nodeSize / 4)
                .build());


        nodes.forEach(node -> {
            List<SomniaDHTKademliaNode> newList = new ArrayList<>(nodes);
            newList.remove(node);
            for (SomniaDHTKademliaNode otherNode : newList) {
                try {
                    node.getRoutingTable().update(otherNode);
                } catch (FullBucketException ignore) {}
            }
        });

        SomniaKey key = SomniaKey.builder()
                .hash(BigInteger.valueOf(2))
                .key(BigInteger.valueOf(2))
                .name(SampleSomniaEntity3.class.getName())
                .build();

        SomniaValue value = SomniaValue.builder()
                .data(objectMapper.valueToTree(SampleData.builder()
                        .integerVal(2)
                        .stringVal("store on node 2")
                        .build()))
                .build();


        StoreAnswer<BigInteger, SomniaKey> storeAnswer = nodes.get(0).store(
                key,
                value
        ).get();

        Thread.sleep(this.timeout);

        AtomicInteger counter = new AtomicInteger();

        this.nodes.forEach(node -> {
            if (node.getKademliaRepository().contains(key)) {
                counter.incrementAndGet();
            }
        });

        Assertions.assertTrue(counter.get() > (this.nodeSize / 4));
        log.info("Distributed data to " + counter.get() + "s of nodes with network of size " + this.nodeSize + " and per-node distribution of " + 20);

        LookupAnswer<BigInteger, SomniaKey, SomniaValue> getAnswer = nodes.get(0).lookup(key).get();
        Assertions.assertTrue(getAnswer.getValue().isExists());
        Assertions.assertEquals(getAnswer.getValue().getCount(), 1);
    }
}
