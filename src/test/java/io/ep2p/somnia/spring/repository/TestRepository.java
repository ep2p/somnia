package io.ep2p.somnia.spring.repository;

import io.ep2p.somnia.config.dynamic.DynamicRepository;
import io.ep2p.somnia.spring.mock.SampleData;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity;
import io.ep2p.somnia.storage.SomniaRepository;

@DynamicRepository(through = SampleSomniaEntity.class)
public interface TestRepository extends SomniaRepository<SampleData> {
}
