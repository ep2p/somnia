package io.ep2p.somnia.model;

import lombok.*;

/**
 * @param <K> Key ID for data to look up of insert
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SomniaKey<K> {
    private K key;
    private String entity;
    private K node;
}
