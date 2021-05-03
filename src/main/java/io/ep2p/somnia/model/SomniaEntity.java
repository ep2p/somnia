package io.ep2p.somnia.model;

import lombok.*;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
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
public abstract class SomniaEntity<D extends Serializable> implements GenericObj {
    private D data;
    private BigInteger key;
    private Date creationDate;
}
