package io.ep2p.somnia.storage;

import io.ep2p.somnia.exception.InvalidQueryException;
import io.ep2p.somnia.model.RepositoryResponse;
import io.ep2p.somnia.model.SomniaEntity;
import io.ep2p.somnia.model.query.Query;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Dynamic Repository Interface
 * @param <D> Data type
 * @param <T> Entity to work with the data through it (SomniaEntity<D>)
 */
public interface SomniaRepository<D extends Serializable, T extends SomniaEntity<?>> {
    RepositoryResponse<D> save(BigInteger id, D data);
    RepositoryResponse<D> findOne(BigInteger id);
    RepositoryResponse<D> findAll(BigInteger id);
    RepositoryResponse<D> find(BigInteger id, Query query, long offset, int limit) throws InvalidQueryException;
    RepositoryResponse<D> find(BigInteger id, long offset, int limit);
}
