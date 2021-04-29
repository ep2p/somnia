package io.ep2p.somnia.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @param <D> Serializable data object type
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class SomniaEntity<D extends Serializable> {
    private D data;
    private BigInteger key;
    private Date creationDate;
    private Set<BigInteger> owners = new HashSet<>();

    public void addOwner(BigInteger owner){
        this.owners.add(owner);
    }
}
