package io.ep2p.somnia.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.ep2p.kademlia.connection.NodeConnectionApi;
import io.ep2p.kademlia.node.KademliaRepository;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.config.properties.SomniaBaseConfigProperties;
import io.ep2p.somnia.config.properties.SomniaDecentralizedConfigProperties;
import io.ep2p.somnia.config.serialization.ExternalNodeDeserializer;
import io.ep2p.somnia.config.serialization.ExternalNodeSerializer;
import io.ep2p.somnia.decentralized.*;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.service.EntityManagerRegisterer;
import io.ep2p.somnia.service.HashGenerator;
import io.ep2p.somnia.storage.DefaultInMemoryStorage;
import io.ep2p.somnia.storage.MongoStorage;
import io.ep2p.somnia.storage.Storage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.math.BigInteger;

@Configuration
@EnableConfigurationProperties({SomniaBaseConfigProperties.class, SomniaDecentralizedConfigProperties.class})
@EnableMongoRepositories
@Import(EntityManagerRegisterer.class)
public class SomniaAutoConfiguration {

    @Bean("somniaSimpleModule")
    public SimpleModule somniaSimpleModule(){
        SimpleModule module = new SimpleModule();
        module.addSerializer(ExternalNode.class, new ExternalNodeSerializer());
        module.addDeserializer(ExternalNode.class, new ExternalNodeDeserializer());
        return module;
    }

    @Bean({"objectMapper", "somniaObjectMapper"})
    @DependsOn("somniaSimpleModule")
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper(SimpleModule somniaSimpleModule){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(somniaSimpleModule);
        return objectMapper;
    }

    @Bean("somniaDecentralizedConfig")
    public Config somniaDecentralizedConfig(SomniaDecentralizedConfigProperties somniaDecentralizedConfigProperties){
        return Config.builder()
                .minimumDistribution(somniaDecentralizedConfigProperties.getMinimumDistribution())
                .forceStore(somniaDecentralizedConfigProperties.isForceStore())
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
    @DependsOn("objectMapper")
    public Storage inMemoryStorage(ObjectMapper objectMapper){
        return new DefaultInMemoryStorage(objectMapper);
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
        return new SomniaKademliaRepository(somniaEntityManager, inMemoryStorage, mongoStorage);
    }

    @Bean(value = "somniaKademliaSyncRepositoryNode", initMethod = "start")
    @DependsOn({"somniaNodeId", "routingTable", "somniaConnectionInfo", "nodeConnectionApi", "somniaKademliaRepository", "somniaEntityManager", "somniaDecentralizedConfig"})
    @ConditionalOnMissingBean(name = "somniaKademliaSyncRepositoryNode", value = SomniaKademliaSyncRepositoryNode.class)
    public SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode(
            BigInteger somniaNodeId,
            RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>> routingTable,
            SomniaConnectionInfo somniaConnectionInfo,
            NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
            KademliaRepository<SomniaKey, SomniaValue> somniaKademliaRepository,
            SomniaEntityManager somniaEntityManager, Config somniaDecentralizedConfig){
        return new SomniaKademliaSyncRepositoryNode(somniaNodeId, routingTable, nodeConnectionApi, somniaConnectionInfo,  somniaKademliaRepository, somniaEntityManager, somniaDecentralizedConfig);
    }

    @Bean("somniaHashGenerator")
    public HashGenerator hashGenerator(){
        return new HashGenerator.DefaultHashGenerator();
    }

    @Bean
    public EntityManagerRegisterer entityManagerRegisterer(MongoTemplate mongoTemplate, SomniaEntityManager somniaEntityManager, SomniaBaseConfigProperties somniaBaseConfigProperties, ApplicationContext applicationContext){
        return new EntityManagerRegisterer(mongoTemplate, somniaEntityManager, somniaBaseConfigProperties, applicationContext);
    }

}
