package io.ep2p.somnia.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.NodeConnectionApi;
import io.ep2p.kademlia.node.KademliaRepository;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.config.properties.SomniaBaseConfigProperties;
import io.ep2p.somnia.config.properties.SomniaDecentralizedConfigProperties;
import io.ep2p.somnia.config.properties.SomniaKademliaSettingsProperties;
import io.ep2p.somnia.config.serialization.ExternalNodeDeserializer;
import io.ep2p.somnia.config.serialization.ExternalNodeSerializer;
import io.ep2p.somnia.decentralized.*;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.service.DefaultRedistributionTaskHandler;
import io.ep2p.somnia.service.EntityManagerRegisterer;
import io.ep2p.somnia.service.HashGenerator;
import io.ep2p.somnia.service.RedistributionTaskHandler;
import io.ep2p.somnia.storage.DefaultInMemoryStorage;
import io.ep2p.somnia.storage.MongoStorage;
import io.ep2p.somnia.storage.Storage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.math.BigInteger;

@Configuration
@EnableConfigurationProperties({SomniaBaseConfigProperties.class, SomniaDecentralizedConfigProperties.class, SomniaKademliaSettingsProperties.class})
@EnableMongoRepositories
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
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.registerModule(somniaSimpleModule);
        return objectMapper;
    }

    @Bean("somniaDecentralizedConfig")
    public SomniaStorageConfig somniaDecentralizedConfig(SomniaDecentralizedConfigProperties somniaDecentralizedConfigProperties){
        return SomniaStorageConfig.builder()
                .minimumDistribution(somniaDecentralizedConfigProperties.getMinimumDistribution())
                .forceStore(somniaDecentralizedConfigProperties.isForceStore())
                .build();
    }

    @Bean("somniaHashGenerator")
    public HashGenerator hashGenerator(){
        return new HashGenerator.DefaultHashGenerator();
    }

    @Bean("mongoStorage")
    @ConditionalOnMissingBean(name = "mongoStorage")
    @DependsOn({"mongoTemplate", "objectMapper", "hashGenerator"})
    public Storage mongoStorage(MongoTemplate mongoTemplate, ObjectMapper objectMapper, HashGenerator hashGenerator){
        return new MongoStorage(mongoTemplate, objectMapper, hashGenerator);
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

    @Bean("redistributionTaskHandler")
    @DependsOn({"somniaKademliaRepository"})
    public RedistributionTaskHandler redistributionTaskHandler(KademliaRepository<SomniaKey, SomniaValue> somniaKademliaRepository) {
        return new DefaultRedistributionTaskHandler();
    }

    @Bean(value = "somniaNodeSettings")
    public NodeSettings somniaNodeSettings(SomniaKademliaSettingsProperties somniaKademliaSettingsProperties){
        return NodeSettings.builder()
                .maximumLastSeenAgeToConsiderAlive(somniaKademliaSettingsProperties.getMaximumLastSeenAgeToConsiderAlive())
                .findNodeSize(somniaKademliaSettingsProperties.getFindNodeSize())
                .joinBucketQueries(somniaKademliaSettingsProperties.getJoinBucketQueries())
                .bucketSize(somniaKademliaSettingsProperties.getBucketSize())
                .referencedNodesUpdatePeriod(somniaKademliaSettingsProperties.getReferencedNodesUpdatePeriod())
                .identifierSize(somniaKademliaSettingsProperties.getIdentifierSize())
                .alpha(somniaKademliaSettingsProperties.getAlpha())
                .storeTimeout(somniaKademliaSettingsProperties.getStoreTimeout())
                .bootstrapNodeCallTimeout(somniaKademliaSettingsProperties.getBootstrapNodeCallTimeout())
                .enabledRepublishing(false)
                .build();
    }

    @Bean(value = "somniaKademliaSyncRepositoryNode", initMethod = "start")
    @DependsOn({"somniaNodeId", "routingTable", "somniaConnectionInfo", "nodeConnectionApi", "somniaKademliaRepository", "somniaEntityManager", "somniaDecentralizedConfig", "redistributionTaskHandler"})
    @ConditionalOnMissingBean(name = "somniaKademliaSyncRepositoryNode", value = SomniaKademliaSyncRepositoryNode.class)
    public SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode(
            BigInteger somniaNodeId,
            RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>> routingTable,
            NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi,
            SomniaConnectionInfo somniaConnectionInfo,
            NodeSettings somniaNodeSettings,
            KademliaRepository<SomniaKey, SomniaValue> somniaKademliaRepository,
            SomniaEntityManager somniaEntityManager, SomniaStorageConfig somniaDecentralizedSomniaStorageConfig,
            RedistributionTaskHandler redistributionTaskHandler){
        SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode = new SomniaKademliaSyncRepositoryNode(somniaNodeId, routingTable, nodeConnectionApi, somniaConnectionInfo, somniaNodeSettings, somniaKademliaRepository, somniaEntityManager, somniaDecentralizedSomniaStorageConfig, redistributionTaskHandler);
        redistributionTaskHandler.init(somniaKademliaSyncRepositoryNode);
        return somniaKademliaSyncRepositoryNode;
    }

    @Bean
    public EntityManagerRegisterer entityManagerRegisterer(MongoTemplate mongoTemplate, SomniaEntityManager somniaEntityManager, SomniaBaseConfigProperties somniaBaseConfigProperties, ApplicationContext applicationContext){
        return new EntityManagerRegisterer(mongoTemplate, somniaEntityManager, somniaBaseConfigProperties, applicationContext);
    }

}
