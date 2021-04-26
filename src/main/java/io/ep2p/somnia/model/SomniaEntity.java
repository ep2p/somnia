package io.ep2p.somnia.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    private Set<ID> owners = new HashSet<>();

    public void addOwner(ID owner){
        this.owners.add(owner);
    }
}
