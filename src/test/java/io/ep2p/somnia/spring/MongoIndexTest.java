package io.ep2p.somnia.spring;

import io.ep2p.somnia.config.SomniaAutoConfiguration;
import io.ep2p.somnia.config.properties.SomniaBaseConfigProperties;
import io.ep2p.somnia.spring.configuration.SomniaTestConfiguration;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SomniaTestConfiguration.class, SomniaAutoConfiguration.class})
@ActiveProfiles({"default", "test"})
@Slf4j
public class MongoIndexTest {

    @Test
    public void testIndex(@Autowired MongoTemplate mongoTemplate, @Autowired SomniaBaseConfigProperties somniaBaseConfigProperties){
        List<IndexInfo> indexInfoList = mongoTemplate.indexOps(SampleSomniaEntity.class).getIndexInfo();
        Assertions.assertTrue(indexInfoList.size() > 0);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        for (IndexInfo indexInfo : indexInfoList) {
            if (indexInfo.getName().equals(somniaBaseConfigProperties.getMongoKeyIndexName())) {
                atomicBoolean.set(true);
            }
        }

        Assertions.assertTrue(atomicBoolean.get());
    }
}
