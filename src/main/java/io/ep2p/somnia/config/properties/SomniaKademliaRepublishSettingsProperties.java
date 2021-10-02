package io.ep2p.somnia.config.properties;

import io.ep2p.kademlia.NodeSettings;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "somnia.config.kademlia.republish")
@Getter
@Setter
public class SomniaKademliaRepublishSettingsProperties extends NodeSettings.RepublishSettings {
}
