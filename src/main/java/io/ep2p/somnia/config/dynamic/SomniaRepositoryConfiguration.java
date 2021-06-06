package io.ep2p.somnia.config.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.somnia.decentralized.SomniaEntityManager;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;
import io.ep2p.somnia.service.HashGenerator;
import io.ep2p.somnia.service.SomniaRepositoryProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;


public class SomniaRepositoryConfiguration {

    @Bean
    @DependsOn({"somniaKademliaSyncRepositoryNode", "somniaEntityManager", "somniaHashGenerator", "somniaObjectMapper"})
    public SomniaRepositoryProxy somniaRepositoryProxy(SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode, SomniaEntityManager somniaEntityManager, HashGenerator somniaHashGenerator, ObjectMapper somniaObjectMapper){
        return new SomniaRepositoryProxy(somniaKademliaSyncRepositoryNode, somniaEntityManager, somniaHashGenerator, somniaObjectMapper);
    }

    @Bean(name = "somniaRepositoryProxyBeanFactory")
    @DependsOn("somniaRepositoryProxy")
    public SomniaRepositoryProxyBeanFactory somniaRepositoryProxyBeanFactory(SomniaRepositoryProxy somniaRepositoryProxy) {
        return new SomniaRepositoryProxyBeanFactory(somniaRepositoryProxy);
    }

}
