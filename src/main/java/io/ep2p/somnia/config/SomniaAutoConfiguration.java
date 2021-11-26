package io.ep2p.somnia.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.node.KeyHashGenerator;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.kademlia.repository.KademliaRepository;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.config.properties.SomniaBaseConfigProperties;
import io.ep2p.somnia.config.properties.SomniaDecentralizedConfigProperties;
import io.ep2p.somnia.config.properties.SomniaKademliaSettingsProperties;
import io.ep2p.somnia.config.serialization.ExternalNodeDeserializer;
import io.ep2p.somnia.config.serialization.ExternalNodeSerializer;
import io.ep2p.somnia.decentralized.*;
import io.ep2p.somnia.decentralized.protocol.SomniaMessageType;
import io.ep2p.somnia.decentralized.protocol.handler.ChunkRequestMessageHandler;
import io.ep2p.somnia.decentralized.protocol.handler.RepublishRequestMessageHandler;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.service.DistributionJobManager;
import io.ep2p.somnia.service.EntityManagerRegisterer;
import io.ep2p.somnia.service.HashGenerator;
import io.ep2p.somnia.service.SomniaDistributionJobManager;
import io.ep2p.somnia.storage.DefaultCacheStorage;
import io.ep2p.somnia.storage.Storage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.math.BigInteger;

@Configuration
@EnableConfigurationProperties(
    {
        SomniaBaseConfigProperties.class,
        SomniaDecentralizedConfigProperties.class,
        SomniaKademliaSettingsProperties.class
    }
)
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
    public SomniaStorageConfig somniaDecentralizedConfig(SomniaDecentralizedConfigProperties somniaDecentralizedConfigProperties){
        return SomniaStorageConfig.builder()
                .minimumDistribution(somniaDecentralizedConfigProperties.getMinimumDistribution())
                .forceStore(somniaDecentralizedConfigProperties.isForceStore())
                .build();
    }

    @Bean("cacheStorage")
    @ConditionalOnMissingBean(name = "cacheStorage")
    @DependsOn("objectMapper")
    public Storage cacheStorage(ObjectMapper objectMapper){
        return new DefaultCacheStorage(objectMapper);
    }

    @Bean("somniaEntityManager")
    @ConditionalOnMissingBean(SomniaEntityManager.class)
    public SomniaEntityManager somniaEntityManager(){
        return new DefaultSomniaEntityManager();
    }

    @Bean("somniaKademliaRepository")
    @ConditionalOnMissingBean(name = "somniaKademliaRepository")
    @DependsOn({"somniaEntityManager", "databaseStorage", "cacheStorage"})
    public KademliaRepository<SomniaKey, SomniaValue> kademliaRepository(SomniaEntityManager somniaEntityManager, Storage databaseStorage, Storage cacheStorage){
        return new SomniaKademliaRepository(somniaEntityManager, cacheStorage, databaseStorage);
    }

    @Bean(value = "somniaKeyHashGenerator")
    @ConditionalOnMissingBean(KeyHashGenerator.class)
    public KeyHashGenerator<BigInteger, SomniaKey> somniaKeyHashGenerator(){
        return new KeyHashGenerator<BigInteger, SomniaKey>() {
            @Override
            public BigInteger generateHash(SomniaKey key) {
                return key.getKey();
            }
        };
    }

    @Bean("chunkMessageHandler")
    @ConditionalOnMissingBean(name = "chunkMessageHandler", value = ChunkRequestMessageHandler.class)
    @DependsOn({"somniaKademliaRepository"})
    public ChunkRequestMessageHandler chunkRequestMessageHandler(SomniaKademliaRepository somniaKademliaRepository){
        return new ChunkRequestMessageHandler(somniaKademliaRepository);
    }

    @Bean(value = "distributionJobManager", initMethod = "start")
    @ConditionalOnMissingBean(name = "distributionJobManager", value = DistributionJobManager.class)
    public DistributionJobManager distributionJobManager(){
        return new SomniaDistributionJobManager();
    }

    @Bean("republishRequestMessageHandler")
    @ConditionalOnMissingBean(name = "republishRequestMessageHandler", value = RepublishRequestMessageHandler.class)
    @DependsOn({"somniaKeyHashGenerator", "distributionJobManager"})
    public RepublishRequestMessageHandler republishRequestMessageHandler(KeyHashGenerator<BigInteger, SomniaKey> somniaKeyHashGenerator, DistributionJobManager distributionJobManager){
        return new RepublishRequestMessageHandler(somniaKeyHashGenerator, distributionJobManager);
    }

    @Bean(value = "somniaDHTKademliaNode", initMethod = "start")
    @DependsOn({"somniaNodeId", "routingTable", "somniaConnectionInfo", "somniaMessageSender", "somniaKademliaRepository", "somniaKeyHashGenerator", "somniaEntityManager", "somniaDecentralizedConfig", "chunkRequestMessageHandler", "republishRequestMessageHandler"})
    @ConditionalOnMissingBean(name = "somniaDHTKademliaNode", value = SomniaDHTKademliaNode.class)
    public SomniaDHTKademliaNode somniaDHTKademliaNode(
            BigInteger somniaNodeId,
            RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>> routingTable,
            MessageSender<BigInteger, SomniaConnectionInfo> somniaMessageSender,
            SomniaConnectionInfo somniaConnectionInfo,
            SomniaKademliaSettingsProperties somniaKademliaSettingsProperties,
            KademliaRepository<SomniaKey, SomniaValue> somniaKademliaRepository,
            KeyHashGenerator<BigInteger, SomniaKey> somniaKeyHashGenerator,
            SomniaEntityManager somniaEntityManager,
            SomniaStorageConfig somniaDecentralizedSomniaStorageConfig,
            ChunkRequestMessageHandler chunkRequestMessageHandler,
            RepublishRequestMessageHandler republishRequestMessageHandler){
        SomniaDHTKademliaNode somniaDHTKademliaNode = new SomniaDHTKademliaNode(somniaNodeId, somniaConnectionInfo, routingTable, somniaMessageSender, somniaKademliaSettingsProperties, somniaKademliaRepository, somniaKeyHashGenerator, somniaEntityManager, somniaDecentralizedSomniaStorageConfig);
        somniaDHTKademliaNode.registerMessageHandler(SomniaMessageType.REPUBLISH_CHUNK_REQUEST, chunkRequestMessageHandler);
        somniaDHTKademliaNode.registerMessageHandler(SomniaMessageType.REPUBLISH_REQUEST, republishRequestMessageHandler);
        return somniaDHTKademliaNode;
    }

    @Bean("somniaHashGenerator")
    public HashGenerator hashGenerator(){
        return new HashGenerator.DefaultHashGenerator();
    }

    @Bean
    public EntityManagerRegisterer entityManagerRegisterer(SomniaEntityManager somniaEntityManager, SomniaBaseConfigProperties somniaBaseConfigProperties, ApplicationContext applicationContext){
        return new EntityManagerRegisterer(somniaEntityManager, somniaBaseConfigProperties, applicationContext);
    }

}
