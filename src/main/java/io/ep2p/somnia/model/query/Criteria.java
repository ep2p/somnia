package io.ep2p.somnia.model.query;

import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Criteria<V extends Serializable> implements Serializable {

    public Criteria(String field) {
        this.field = field;
    }

    private String field;
    private String operation;
    private V value;
}
