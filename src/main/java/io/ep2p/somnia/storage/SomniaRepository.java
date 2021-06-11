package io.ep2p.somnia.storage;

import io.ep2p.somnia.model.RepositoryResponse;
import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.math.BigInteger;

public interface SomniaRepository<D extends Serializable> {
    RepositoryResponse<D> save(BigInteger id, D data);
    RepositoryResponse<D> findOne(BigInteger id);
    RepositoryResponse<D> findAll(BigInteger id);
    RepositoryResponse<D> find(BigInteger id, Query query, long offset, int limit);
    RepositoryResponse<D> find(BigInteger id, long offset, int limit);
}
