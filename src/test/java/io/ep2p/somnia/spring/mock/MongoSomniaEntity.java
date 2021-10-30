package io.ep2p.somnia.spring.mock;

import io.ep2p.somnia.model.SomniaEntity;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Getter
@Setter
public class MongoSomniaEntity<D extends Serializable> extends SomniaEntity<ObjectId, D> {
    @Id
    private ObjectId id;
}
