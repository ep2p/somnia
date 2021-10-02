package io.ep2p.somnia.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "somnia.config.kademlia")
@Getter
@Setter
public class SomniaKademliaSettingsProperties {
    private long bootstrapNodeCallTimeout = 100;
    private long storeTimeout = 20;
    private int alpha = 3;
    private int identifierSize = 128;
    private int referencedNodesUpdatePeriod = 30;
    /* Maximum size of the buckets */
    private int bucketSize = 20;
    private int findNodeSize = 20;
    private int joinBucketQueries = 1;
    private int maximumLastSeenAgeToConsiderAlive = 20;
    private boolean enabledRepublishing = false;
}
