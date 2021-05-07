package io.ep2p.somnia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.somnia.config.properties.SomniaConfigurationProperties;
import io.ep2p.somnia.decentralized.Config;
import io.ep2p.somnia.decentralized.DefaultSomniaEntityManager;
import io.ep2p.somnia.decentralized.SomniaEntityManager;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;
import io.ep2p.somnia.storage.DefaultInMemoryStorage;
import io.ep2p.somnia.storage.MongoStorage;
import io.ep2p.somnia.storage.Storage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

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

    /*@Bean()
    @DependsOn({"somniaEntityManager", "somniaDecentralizedConfig"})
    public SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode(SomniaEntityManager somniaEntityManager, Config somniaDecentralizedConfig){
        return new SomniaKademliaSyncRepositoryNode()
    }*/

}
