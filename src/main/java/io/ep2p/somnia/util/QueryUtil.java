package io.ep2p.somnia.util;

import io.ep2p.somnia.model.SomniaKey;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class QueryUtil {
    public static Query generateQuery(SomniaKey somniaKey){
        Query query;
        if(somniaKey.getMeta().getQuery() != null){
            Document passedQueryDocument = Document.parse(somniaKey.getMeta().getQuery());

            Document document = new Document();
            document.put("key", somniaKey.getKeyAsString());
            passedQueryDocument.forEach(document::put);

            query = new BasicQuery(document);
        }else {
            query = new Query(Criteria.where("key").is(somniaKey.getKeyAsString()));
        }
        return query;
    }
}
