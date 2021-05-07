package io.ep2p.somnia.model;

import io.ep2p.somnia.annotation.SomniaDocument;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

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
    @Id
    private ObjectId id;
    private D data;
    private BigInteger key;
    private Date creationDate;

    public SomniaDocument getSomniaDocument(){
        SomniaDocument somniaDocument = this.getClass().getAnnotation(SomniaDocument.class);
        if (somniaDocument == null)
            throw new RuntimeException("SomniaEntity sub-class should be marked as @SomniaDocument");
        return somniaDocument;
    }
}
