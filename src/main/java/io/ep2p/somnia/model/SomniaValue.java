package io.ep2p.somnia.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SomniaValue {
    private List<BigInteger> owners;
    private JsonNode data;
}
