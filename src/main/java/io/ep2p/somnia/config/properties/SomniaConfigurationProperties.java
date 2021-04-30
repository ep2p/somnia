package io.ep2p.somnia.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "somnia.decentralized.config")
@Getter
@Setter
public class SomniaConfigurationProperties {
    private int minimumDistribtion;
}
