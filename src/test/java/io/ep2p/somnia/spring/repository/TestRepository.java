package io.ep2p.somnia.spring.repository;

import io.ep2p.somnia.config.dynamic.DynamicRepository;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity;

@DynamicRepository(through = SampleSomniaEntity.class)
public interface TestRepository {
    void dosomething();
}
