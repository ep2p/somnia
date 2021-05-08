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
    @Builder.Default
    private Meta meta = new Meta();

    public synchronized void incrementDistribution(){
        this.distributions++;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Meta {
        private int offset = 0;
        private int limit = 20;
        private String query;
    }

    public String getKeyAsString(){
        BigInteger key = getKey();
        if (key != null)
            return key.toString();
        return "";
    }
}
