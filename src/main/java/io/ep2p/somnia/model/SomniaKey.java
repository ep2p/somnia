package io.ep2p.somnia.model;

import lombok.*;

import java.math.BigInteger;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SomniaKey {
    private BigInteger key;
    private String name;
    private BigInteger hash;
    private BigInteger requester;
    @Builder.Default
    private int distributions = 0;

    public synchronized void incrementDistribution(){
        this.distributions++;
    }
}
