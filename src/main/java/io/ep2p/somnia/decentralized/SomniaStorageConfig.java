package io.ep2p.somnia.decentralized;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SomniaStorageConfig {
    @Builder.Default
    private int perNodeDistribution = 3;
    @Builder.Default
    private int minimumDistribution = 20;
    @Builder.Default
    private boolean forceStore = true;

    public final int getMaximumDistribution(){
        return getMinimumDistribution() + 10;
    }
}
