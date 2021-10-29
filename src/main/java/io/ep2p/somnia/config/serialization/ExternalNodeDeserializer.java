package io.ep2p.somnia.config.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.kademlia.node.Node;
import io.ep2p.kademlia.node.external.BigIntegerExternalNode;
import io.ep2p.kademlia.node.external.ExternalNode;
import io.ep2p.somnia.decentralized.SomniaConnectionInfo;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

public class ExternalNodeDeserializer extends JsonDeserializer<ExternalNode<BigInteger, SomniaConnectionInfo>> {
    private final ObjectMapper objectMapper;

    public ExternalNodeDeserializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ExternalNode<BigInteger, SomniaConnectionInfo> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode jsonNode = deserializationContext.readTree(jsonParser);
        SomniaConnectionInfo connectionInfo = objectMapper.readValue(jsonNode.get("connectionInfo").toString(), SomniaConnectionInfo.class);
        return new BigIntegerExternalNode<>(
                new Node<BigInteger, SomniaConnectionInfo>() {
                    @Override
                    public SomniaConnectionInfo getConnectionInfo() {
                        return connectionInfo;
                    }

                    @Override
                    public BigInteger getId() {
                        return jsonNode.get("id").bigIntegerValue();
                    }

                    @Override
                    public void setLastSeen(Date date) {
                        new Date();
                    }
                }, jsonNode.get("distance").bigIntegerValue()
        );
    }
}

