package io.ep2p.somnia.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "somnia.config.decentralized")
@Getter
@Setter
public class SomniaDecentralizedConfigProperties {
    private int minimumDistribution;
    private boolean forceStore = true;
}
