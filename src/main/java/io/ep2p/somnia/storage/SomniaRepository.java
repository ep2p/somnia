package io.ep2p.somnia.storage;

import io.ep2p.somnia.model.GenericObj;
import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface SomniaRepository<D extends Serializable> extends GenericObj {
    boolean save(BigInteger id, D data);
    Optional<D> findOne(BigInteger id);
    List<D> findAll(BigInteger id);
    List<D> find(BigInteger id, Query query, long offset, int limit);
}
