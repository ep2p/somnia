package io.ep2p.somnia.config.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.kademlia.node.external.BigIntegerExternalNode;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;

import java.io.IOException;
import java.math.BigInteger;

public class ExternalNodeDeserializer extends JsonDeserializer<ExternalNode<BigInteger, SomniaConnectionInfo>> {
    private final ObjectMapper objectMapper;

    public ExternalNodeDeserializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ExternalNode<BigInteger, SomniaConnectionInfo> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode jsonNode = deserializationContext.readTree(jsonParser);
        SomniaConnectionInfo connectionInfo = objectMapper.readValue(jsonNode.get("connectionInfo").toString(), SomniaConnectionInfo.class);
        BigIntegerExternalNode<SomniaConnectionInfo> bigIntegerExternalNode = new BigIntegerExternalNode<>();
        bigIntegerExternalNode.setConnectionInfo(connectionInfo);
        bigIntegerExternalNode.setId(jsonNode.get("id").bigIntegerValue());
        bigIntegerExternalNode.setDistance(jsonNode.get("distance").bigIntegerValue());
        return bigIntegerExternalNode;
    }
}

