package io.ep2p.somnia.model;

import lombok.*;

/**
 * @param <ID> Key ID for data to look up of insert
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SomniaKey<ID extends Number> {
    private ID key;
    private String entity;
    private ID node;
}
