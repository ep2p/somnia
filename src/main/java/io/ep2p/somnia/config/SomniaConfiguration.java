package io.ep2p.somnia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.somnia.config.properties.SomniaConfigurationProperties;
import io.ep2p.somnia.decentralized.Config;
import io.ep2p.somnia.storage.DefaultInMemoryStorage;
import io.ep2p.somnia.storage.MongoStorage;
import io.ep2p.somnia.storage.Storage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@EnableConfigurationProperties({SomniaConfigurationProperties.class})
public class SomniaConfiguration {

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

    //todo: SomniaEntityManager bean

    /*@Bean()
    @DependsOn({"somniaEntityManager", "somniaDecentralizedConfig"})
    public SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode(){
        return new SomniaKademliaSyncRepositoryNode()
    }*/

}
