package io.ep2p.somnia.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SomniaValue implements Serializable {
    @Builder.Default
    private long count = 1;
    private JsonNode data;
    @Builder.Default
    private boolean exists = true;
}
