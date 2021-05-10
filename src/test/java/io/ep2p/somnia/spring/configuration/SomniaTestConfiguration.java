package io.ep2p.somnia.spring.configuration;

import com.github.ep2p.kademlia.connection.ConnectionInfo;
import com.github.ep2p.kademlia.connection.NodeConnectionApi;
import com.github.ep2p.kademlia.table.BigIntegerRoutingTable;
import com.github.ep2p.kademlia.table.Bucket;
import com.github.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.config.properties.SomniaBaseConfigProperties;
import io.ep2p.somnia.config.properties.SomniaDecentralizedConfigProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;

@TestConfiguration
@EnableAutoConfiguration
public class SomniaTestConfiguration {

    @Bean("somniaDecentralizedConfigProperties")
    public SomniaDecentralizedConfigProperties decentralizedConfigProperties(){
        return new SomniaDecentralizedConfigProperties();
    }

    @Bean("somniaBaseConfigProperties")
    public SomniaBaseConfigProperties somniaBaseConfigProperties(){
        return SomniaBaseConfigProperties.builder().basePackage("io.ep2p.somnia").build();
    }

    @Bean
    public BigInteger somniaNodeId(){
        return BigInteger.valueOf(100);
    }

    @Bean
    public RoutingTable<BigInteger, ConnectionInfo, Bucket<BigInteger, ConnectionInfo>> routingTable(BigInteger somniaNodeId){
        return new BigIntegerRoutingTable<>(somniaNodeId);
    }

    @Bean
    public ConnectionInfo connectionInfo(){
        return new ConnectionInfo(){};
    }

    @Bean
    public NodeConnectionApi<BigInteger, ConnectionInfo> nodeConnectionApi(){
        return new LocalNodeConnectionApi<BigInteger>();
    }

}
