package io.ep2p.somnia.storage;

import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface SomniaRepository<D extends Serializable> {
    void save(D data);
    Optional<D> findOne(BigInteger id);
    List<D> findAll(BigInteger id);
    List<D> find(BigInteger id, Query query);
}
