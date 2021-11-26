package io.ep2p.somnia.model.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    @Builder.Default
    private String field = "id";
    @Builder.Default
    private Direction direction = Direction.ASC;

    public enum Direction {
        ASC, DESC
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return field.equals(order.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field);
    }
}
