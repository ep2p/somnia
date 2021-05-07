package io.ep2p.somnia.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

/**
 * @param <D> Serializable data object type
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class SomniaEntity<D extends Serializable> implements GenericObj {
    @Id
    private ObjectId id;
    private D data;
    private String key;
    private Date creationDate;
}
