package io.ep2p.somnia.spring;

import io.ep2p.somnia.config.SomniaAutoConfiguration;
import io.ep2p.somnia.model.RepositoryResponse;
import io.ep2p.somnia.spring.configuration.RepositoryEnabledSomniaTestConfiguration;
import io.ep2p.somnia.spring.configuration.SomniaTestConfiguration;
import io.ep2p.somnia.spring.mock.SampleData;
import io.ep2p.somnia.spring.repository.TestRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigInteger;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SomniaTestConfiguration.class, SomniaAutoConfiguration.class, RepositoryEnabledSomniaTestConfiguration.class})
@ActiveProfiles({"default", "test"})
@Slf4j
public class DynamicRepositoryTest {

    @Test
    public void testRepository(@Autowired TestRepository testRepository, @Autowired BigInteger somniaNodeId){
        SampleData sampleData = SampleData.builder()
                .stringVal("A")
                .integerVal(0)
                .build();
        RepositoryResponse<SampleData> response = testRepository.save(BigInteger.valueOf(1), sampleData);
        Assertions.assertTrue(response.isSuccess());
        Assertions.assertEquals(response.getNode(), somniaNodeId);

        RepositoryResponse<SampleData> response_unknown = testRepository.findOne(BigInteger.valueOf(2000));
        Assertions.assertFalse(response_unknown.isSuccess());

        RepositoryResponse<SampleData> response_find_one = testRepository.findOne(BigInteger.valueOf(1));
        Assertions.assertTrue(response_find_one.isSuccess());
        Assertions.assertNotNull(response_find_one.getResult());
        Assertions.assertEquals(response_find_one.getResult(), sampleData);
    }
}
