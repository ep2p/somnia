package io.ep2p.somnia.model.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Query {
    private List<Criteria<?>> criteriaList = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();

    public static Query withCriteria(Criteria<?> criteria){
        Query query = new Query();
        query.addCriteria(criteria);
        return query;
    }

    public synchronized Query addCriteria(Criteria<?> criteria){
        this.criteriaList.add(criteria);
        return this;
    }

    public synchronized Query addOrder(Order order){
        if (!this.orders.contains(order))
            this.orders.add(order);
        return this;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    public static Query fromJson(String body) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(body, Query.class);
    }

}
