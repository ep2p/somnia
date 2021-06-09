package io.ep2p.somnia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryResponse<D> {
    private boolean success;
    private List<D> results;
    private D result;
    private BigInteger node;
}
