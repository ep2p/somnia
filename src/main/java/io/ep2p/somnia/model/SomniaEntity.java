package io.ep2p.somnia.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @param <D> Serializable data object type
 * @param <K> Key type
 * @param <ID> Node ID Types
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public abstract class SomniaEntity<D extends Serializable, K, ID extends Number> {
    private D data;
    private K key;
    private Date creationDate;
}
