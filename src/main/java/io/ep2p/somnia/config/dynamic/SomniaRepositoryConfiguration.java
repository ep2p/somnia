package io.ep2p.somnia.config.dynamic;

import io.ep2p.somnia.decentralized.SomniaEntityManager;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;
import io.ep2p.somnia.service.SomniaRepositoryProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;


public class SomniaRepositoryConfiguration {

    @Bean
    @DependsOn("somniaKademliaSyncRepositoryNode")
    public SomniaRepositoryProxy somniaRepositoryProxy(SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode, SomniaEntityManager somniaEntityManager){
        return new SomniaRepositoryProxy(somniaKademliaSyncRepositoryNode, somniaEntityManager);
    }

    @Bean(name = "somniaRepositoryProxyBeanFactory")
    @DependsOn("somniaRepositoryProxy")
    public SomniaRepositoryProxyBeanFactory somniaRepositoryProxyBeanFactory(SomniaRepositoryProxy somniaRepositoryProxy) {
        return new SomniaRepositoryProxyBeanFactory(somniaRepositoryProxy);
    }

}
