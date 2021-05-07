package io.ep2p.somnia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.node.KademliaRepository;
import com.github.ep2p.kademlia.table.Bucket;
import com.github.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.config.properties.SomniaConfigurationProperties;
import io.ep2p.somnia.decentralized.*;
import io.ep2p.somnia.model.SomniaConnectionInfo;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.storage.DefaultInMemoryStorage;
import io.ep2p.somnia.storage.MongoStorage;
import io.ep2p.somnia.storage.Storage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.math.BigInteger;

@Configuration
@EnableConfigurationProperties({SomniaConfigurationProperties.class})
@EnableMongoRepositories
public class SomniaConfiguration {

    @Bean("objectMapper")
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean("somniaDecentralizedConfig")
    @DependsOn("somniaConfigurationProperties")
    public Config somniaDecentralizedConfig(SomniaConfigurationProperties somniaConfigurationProperties){
        return Config.builder()
                .minimumDistribution(somniaConfigurationProperties.getMinimumDistribtion())
                .build();
    }

    @Bean("mongoStorage")
    @ConditionalOnMissingBean(name = "mongoStorage")
    @DependsOn({"mongoTemplate", "objectMapper"})
    public Storage mongoStorage(MongoTemplate mongoTemplate, ObjectMapper objectMapper){
        return new MongoStorage(mongoTemplate, objectMapper);
    }

    @Bean("inMemoryStorage")
    @ConditionalOnMissingBean(name = "inMemoryStorage")
    public Storage inMemoryStorage(){
        return new DefaultInMemoryStorage();
    }

    @Bean("somniaEntityManager")
    @ConditionalOnMissingBean(SomniaEntityManager.class)
    public SomniaEntityManager somniaEntityManager(){
        return new DefaultSomniaEntityManager();
    }

    @Bean("somniaKademliaRepository")
    @ConditionalOnMissingBean(name = "somniaKademliaRepository")
    @DependsOn({"somniaEntityManager", "mongoStorage", "inMemoryStorage"})
    public KademliaRepository<SomniaKey, SomniaValue> kademliaRepository(SomniaEntityManager somniaEntityManager, Storage mongoStorage, Storage inMemoryStorage){
        return new SomniaKademliaRepository(somniaEntityManager, inMemoryStorage, mongoStorage)
    }

    @Bean("somniaKademliaSyncRepositoryNode")
    @DependsOn({"somniaNodeId", "routingTable", "somniaConnectionInfo", "nodeConnectionApi", "somniaKademliaRepository", "somniaEntityManager", "somniaDecentralizedConfig"})
    @ConditionalOnMissingBean(name = "somniaKademliaSyncRepositoryNode", value = SomniaKademliaSyncRepositoryNode.class)
    public SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode(
            BigInteger somniaNodeId,
            RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>> routingTable,
            SomniaConnectionInfo somniaConnectionInfo,
            NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
            KademliaRepository<SomniaKey, SomniaValue> kademliaRepository,
            SomniaEntityManager somniaEntityManager, Config somniaDecentralizedConfig){
        return new SomniaKademliaSyncRepositoryNode(somniaNodeId, routingTable, nodeConnectionApi, somniaConnectionInfo,  kademliaRepository, somniaEntityManager, somniaDecentralizedConfig);
    }

}
