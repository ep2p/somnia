package io.ep2p.somnia.config.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.ep2p.kademlia.node.external.ExternalNode;

import java.io.IOException;
import java.math.BigInteger;

public class ExternalNodeSerializer extends JsonSerializer<ExternalNode> {

    @Override
    public void serialize(ExternalNode externalNode, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        jgen.writeNumberField("id", (BigInteger) externalNode.getId());
        jgen.writeNumberField("distance", (BigInteger) externalNode.getDistance());
        jgen.writeObjectField("connectionInfo", externalNode.getConnectionInfo());
        jgen.writeEndObject();
    }
}
