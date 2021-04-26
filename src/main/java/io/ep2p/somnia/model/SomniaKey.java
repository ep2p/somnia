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
}
