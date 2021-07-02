package io.ep2p.somnia.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

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

    public SomniaKey makeClone(){
        return SomniaKey.builder()
                .distributions(this.getDistributions())
                .key(this.getKey())
                .name(this.getName())
                .hash(this.getHash())
                .hitNode(this.getHitNode())
                .meta(this.getMeta().makeClone())
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Meta {
        private long offset = 0;
        private int limit = 20;
        private String query;

        public Meta makeClone(){
            return Meta.builder()
                    .limit(this.getLimit())
                    .offset(this.getOffset())
                    .query(this.getQuery())
                    .build();
        }
    }

    public String getKeyAsString(){
        BigInteger key = getKey();
        if (key != null)
            return key.toString();
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SomniaKey somniaKey = (SomniaKey) o;
        return Objects.equals(getKey(), somniaKey.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }
}
