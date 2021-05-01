package io.ep2p.somnia.config;

import io.ep2p.somnia.config.properties.SomniaConfigurationProperties;
import io.ep2p.somnia.decentralized.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

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


    //todo: SomniaEntityManager bean

    /*@Bean()
    @DependsOn({"somniaEntityManager", "somniaDecentralizedConfig"})
    public SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode(){
        return new SomniaKademliaSyncRepositoryNode()
    }*/

}
