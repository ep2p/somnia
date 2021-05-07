package io.ep2p.somnia;


import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.util.QueryUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigInteger;

public class QueryUtilTest {

    @Test
    public void testGenerateQuery(){
        String query = "{\"x\": 1}";
        String json = Query.query(Criteria.where("x").is(1)).getQueryObject().toJson();
        Assertions.assertEquals(query, json, "Converted query doesnt match expected json");

        SomniaKey somniaKey = SomniaKey.builder()
                .key(BigInteger.valueOf(100))
                .meta(SomniaKey.Meta.builder()
                        .query(json)
                        .build())
                .build();

        Assertions.assertEquals(QueryUtil.generateQuery(somniaKey).getQueryObject().toJson(), "{\"key\": \"100\", \"x\": 1}");
    }

}
