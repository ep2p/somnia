package io.ep2p.somnia.config.properties;

import io.ep2p.kademlia.NodeSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "somnia.config.kademlia.republish-settings")
public class SomniaKademliaRepublishSettingsProperties extends NodeSettings.RepublishSettings {
}
