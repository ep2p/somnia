package io.ep2p.somnia.spring;

import io.ep2p.somnia.model.SomniaKey;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class QueryUtil {
    public static Query generateQuery(SomniaKey somniaKey){
        return new Query(Criteria.where("key").is(somniaKey.getKeyAsString()));
    }
}
