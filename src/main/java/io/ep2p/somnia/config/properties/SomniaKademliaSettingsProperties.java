package io.ep2p.somnia.config.properties;

import io.ep2p.kademlia.NodeSettings;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "somnia.config.kademlia")
@Getter
@Setter
public class SomniaKademliaSettingsProperties extends NodeSettings {
    public SomniaKademliaSettingsProperties(NodeSettings nodeSettings) {
        super(nodeSettings.alpha, nodeSettings.identifierSize, nodeSettings.bucketSize, nodeSettings.findNodeSize, nodeSettings.maximumLastSeenAgeToConsiderAlive, nodeSettings.pingScheduleTimeValue, nodeSettings.pingScheduleTimeUnit, nodeSettings.dhtExecutorPoolSize, nodeSettings.dhtScheduledExecutorPoolSize, nodeSettings.maximumStoreAndLookupTimeoutValue, nodeSettings.maximumStoreAndGetTimeoutTimeUnit, nodeSettings.enabledFirstStoreRequestForcePass);
    }

    public SomniaKademliaSettingsProperties() {
    }
}
