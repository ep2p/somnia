package io.ep2p.somnia.spring.configuration;

import io.ep2p.kademlia.NodeSettings;
import io.ep2p.kademlia.connection.MessageSender;
import io.ep2p.kademlia.table.BigIntegerRoutingTable;
import io.ep2p.kademlia.table.Bucket;
import io.ep2p.kademlia.table.RoutingTable;
import io.ep2p.somnia.config.properties.SomniaBaseConfigProperties;
import io.ep2p.somnia.config.properties.SomniaDecentralizedConfigProperties;
import io.ep2p.somnia.config.properties.SomniaKademliaSettingsProperties;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

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

    @Bean("somniaKademliaSettingsProperties")
    public SomniaKademliaSettingsProperties somniaKademliaSettingsProperties(){
        return new SomniaKademliaSettingsProperties(NodeSettings.Default.build());
    }

    @Bean
    @DependsOn("somniaKademliaSettingsProperties")
    public RoutingTable<BigInteger, SomniaConnectionInfo, Bucket<BigInteger, SomniaConnectionInfo>> routingTable(BigInteger somniaNodeId, SomniaKademliaSettingsProperties somniaKademliaSettingsProperties){
        return new BigIntegerRoutingTable<>(somniaNodeId, somniaKademliaSettingsProperties);
    }

    @Bean("somniaConnectionInfo")
    public SomniaConnectionInfo somniaConnectionInfo(){
        return new SomniaConnectionInfo(){};
    }

    @Bean("somniaMessageSender")
    public MessageSender<BigInteger, SomniaConnectionInfo> somniaMessageSender(){
        return new TestMessageSenderAPI<>();
    }

}
