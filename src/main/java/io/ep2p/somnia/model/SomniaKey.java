package io.ep2p.somnia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SomniaKey {
    private BigInteger key;
    private String entity;
    private BigInteger node;
    private BigInteger requester;
}
