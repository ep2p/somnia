package io.ep2p.somnia.spring.configuration;

import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.table.BigIntegerRoutingTable;
import com.github.ep2p.kademlia.table.Bucket;
import com.github.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.config.properties.SomniaConfigurationProperties;
import io.ep2p.somnia.model.SomniaConnectionInfo;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;

@TestConfiguration
@EnableAutoConfiguration
public class SomniaTestConfiguration {

    @Bean
    public SomniaConfigurationProperties somniaConfigurationProperties(){
        return new SomniaConfigurationProperties();
    }

    @Bean
    public BigInteger somniaNodeId(){
        return BigInteger.valueOf(100);
    }

    @Bean
    public RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>> routingTable(BigInteger somniaNodeId){
        return new BigIntegerRoutingTable<>(somniaNodeId);
    }

    @Bean
    public SomniaConnectionInfo somniaConnectionInfo(){
        return new SomniaConnectionInfo();
    }

    @Bean
    public NodeConnectionApi<BigInteger, SomniaConnectionInfo> nodeConnectionApi(){
        return new LocalNodeConnectionApi<BigInteger>();
    }

}
