package io.ep2p.somnia.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigInteger;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SomniaKey implements Serializable {
    private BigInteger key;
    private String name;
    private BigInteger hash;
    private BigInteger hitNode;
    @Builder.Default
    private int distributions = 0;

    public synchronized void incrementDistribution(){
        this.distributions++;
    }
}
