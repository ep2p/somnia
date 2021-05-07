package io.ep2p.somnia;

import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.decentralized.DefaultSomniaEntityManager;
import io.ep2p.somnia.decentralized.SomniaEntityManager;
import io.ep2p.somnia.model.EntityType;
import io.ep2p.somnia.model.SomniaEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class SomniaEntityManagerTest {
    private final SomniaEntityManager somniaEntityManager;

    public SomniaEntityManagerTest() {
        somniaEntityManager = new DefaultSomniaEntityManager();
    }

    @SomniaDocument(inMemory = true, type = EntityType.HIT, uniqueKey = true)
    private static class SampleSomniaEntity extends SomniaEntity<String> {}

    @Test
    public void test() throws ClassNotFoundException {
        this.somniaEntityManager.register(SampleSomniaEntity.class);
        Class<? extends SomniaEntity> classOfName = this.somniaEntityManager.getClassOfName(SampleSomniaEntity.class.getName());
        Assertions.assertEquals(classOfName, SampleSomniaEntity.class);
        Optional<SomniaDocument> documentOfName = this.somniaEntityManager.getDocumentOfName(SampleSomniaEntity.class.getName());
        SomniaDocument somniaDocument = documentOfName.get();
        Assertions.assertNotNull(somniaDocument);
        Assertions.assertTrue(somniaDocument.inMemory());
        Assertions.assertTrue(somniaDocument.uniqueKey());
        Assertions.assertEquals(somniaDocument.type(), EntityType.HIT);
    }

}
