package io.ep2p.somnia.spring.mock;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class SampleData implements Serializable {
    private String stringVal;
    private Integer integerVal;
}
