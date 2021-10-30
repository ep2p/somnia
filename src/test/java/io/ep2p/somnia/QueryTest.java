package io.ep2p.somnia;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.ep2p.somnia.model.query.Criteria;
import io.ep2p.somnia.model.query.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class QueryTest {

    @Test
    public void test() throws JsonProcessingException {
        Query query = Query.withCriteria(Criteria.builder()
                .field("A")
                .operation("equals")
                .value("A")
                .build());

        Query newQuery = Query.fromJson(query.toJson());
        Assertions.assertTrue(newQuery.getCriteriaList().size() > 0);
        Assertions.assertEquals("A", newQuery.getCriteriaList().get(0).getField());

        ArrayList<Criteria<?>> andCriteriaList = new ArrayList<>();
        andCriteriaList.add(Criteria.builder().field("B").operation("equals").value("B").build());
        andCriteriaList.add(Criteria.builder().field("C").operation("equals").value("C").build());

        query.addCriteria(Criteria.builder()
                .operation("and")
                .value(andCriteriaList)
                .build());

        newQuery = Query.fromJson(query.toJson());
        Assertions.assertTrue(newQuery.getCriteriaList().size() > 1);
        Assertions.assertTrue(newQuery.getCriteriaList().get(1).getValue() instanceof List);

    }

}
