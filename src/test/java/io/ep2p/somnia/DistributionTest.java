package io.ep2p.somnia;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.kademlia.Common;
import io.ep2p.kademlia.exception.BootstrapException;
import io.ep2p.kademlia.exception.GetException;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.kademlia.node.KademliaNode;
import io.ep2p.kademlia.node.KademliaNodeListener;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.table.BigIntegerRoutingTable;
import io.ep2p.somnia.decentralized.*;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.spring.configuration.LocalNodeConnectionApi;
import io.ep2p.somnia.spring.mock.SampleData;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity3;
import io.ep2p.somnia.storage.DefaultInMemoryStorage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class DistributionTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<SomniaKademliaSyncRepositoryNode> nodes = new ArrayList<>();
    private final int nodeSize = 100;
    private final long timeout = 32000;

    private void init_network(Config config) throws BootstrapException {
        nodes.clear();
        Common.REFERENCED_NODES_UPDATE_PERIOD_SEC = 5;
        Common.BUCKET_SIZE = 256;
        Common.IDENTIFIER_SIZE = 256;
        Common.ALPHA = 30;


        SomniaEntityManager somniaEntityManager = new DefaultSomniaEntityManager();
        somniaEntityManager.register(SampleSomniaEntity3.class);
        LocalNodeConnectionApi<BigInteger> localNodeConnectionApi = new LocalNodeConnectionApi<>();

        SomniaKademliaSyncRepositoryNode previousNode = null;
        for(int i = 0; i < this.nodeSize; i++){
            SomniaKademliaRepository somniaKademliaRepository = new SomniaKademliaRepository(somniaEntityManager, new DefaultInMemoryStorage(objectMapper), new DefaultInMemoryStorage(objectMapper));
            SomniaKademliaSyncRepositoryNode node = new SomniaKademliaSyncRepositoryNode(BigInteger.valueOf(i), new BigIntegerRoutingTable<>(BigInteger.valueOf(i)), localNodeConnectionApi, new SomniaConnectionInfo(), somniaKademliaRepository, somniaEntityManager, config);
            node.start();
            localNodeConnectionApi.registerNode(node);
            if (previousNode != null)
                node.bootstrap(previousNode);
            nodes.add(node);
            previousNode = node;
        }
    }


    /*
    *  Proof that Somnia can distribute data for at least 1/4 of the network
    */
    @Test
    public void minimumDistributionTest() throws StoreException, BootstrapException, InterruptedException {
        init_network(Config.builder()
                .forceStore(true)
                .perNodeDistribution(20)
                .minimumDistribution(this.nodeSize / 4)
                .build());


        nodes.forEach(node -> {
            List<SomniaKademliaSyncRepositoryNode> newList = new ArrayList<>(nodes);
            newList.remove(node);
            for (SomniaKademliaSyncRepositoryNode otherNode : newList) {
                node.getRoutingTable().update(otherNode);
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
                value,
                true
        );

        Thread.sleep(this.timeout);

        AtomicInteger counter = new AtomicInteger();

        this.nodes.forEach(node -> {
            if (node.getKademliaRepository().contains(key)) {
                counter.incrementAndGet();
            }
        });

        Assertions.assertTrue(counter.get() > (this.nodeSize / 4));
        log.info("Distributed data to " + counter.get() + "s of nodes with network of size " + this.nodeSize + " and per-node distribution of " + 20);
    }
}
