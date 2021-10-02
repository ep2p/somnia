package io.ep2p.somnia.config.properties;

import io.ep2p.kademlia.NodeSettings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "somnia.config.kademlia.republish")
@Data
public class SomniaKademliaRepublishSettingsProperties extends NodeSettings.RepublishSettings {
}
