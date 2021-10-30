package io.ep2p.somnia.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @param <D> Serializable data object type
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"creationDate", "id"})
@ToString
public abstract class SomniaEntity<ID, D extends Serializable> implements GenericObj {
    private ID id;
    private D data;
    private String key;
    private Date creationDate;
}
