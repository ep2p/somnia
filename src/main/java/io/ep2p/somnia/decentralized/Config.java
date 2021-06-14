package io.ep2p.somnia.decentralized;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Config {
    private int minimumDistribution = 20;
    private boolean forceStore = true;
}
