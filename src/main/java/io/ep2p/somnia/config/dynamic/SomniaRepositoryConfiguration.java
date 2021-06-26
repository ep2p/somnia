package io.ep2p.somnia.config.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.somnia.decentralized.SomniaEntityManager;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;
import io.ep2p.somnia.service.HashGenerator;
import io.ep2p.somnia.service.SomniaRepositoryEnhancerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SomniaRepositoryConfiguration {

    @Bean
    @DependsOn({"somniaKademliaSyncRepositoryNode", "somniaEntityManager", "somniaHashGenerator", "somniaObjectMapper"})
    public SomniaRepositoryEnhancerFactory somniaRepositoryEnhancerFactory(SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode, HashGenerator somniaHashGenerator, ObjectMapper somniaObjectMapper, SomniaEntityManager somniaEntityManager){
        return new SomniaRepositoryEnhancerFactory(somniaKademliaSyncRepositoryNode, somniaHashGenerator, somniaObjectMapper, somniaEntityManager);
    }

    @Bean(name = "somniaRepositoryProxyBeanFactory")
    @DependsOn("somniaRepositoryEnhancerFactory")
    public SomniaRepositoryProxyBeanFactory somniaRepositoryProxyBeanFactory(SomniaRepositoryEnhancerFactory somniaRepositoryEnhancerFactory) {
        return new SomniaRepositoryProxyBeanFactory(somniaRepositoryEnhancerFactory);
    }

}
