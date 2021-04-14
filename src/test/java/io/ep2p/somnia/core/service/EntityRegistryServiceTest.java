package io.ep2p.somnia.core.service;

import io.ep2p.somnia.annotation.IgnoreField;
import io.ep2p.somnia.annotation.SomniaEntity;
import io.ep2p.somnia.model.EntityIdentity;
import io.ep2p.somnia.model.StorageMethod;
import io.ep2p.somnia.service.EntityRegistryService;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityRegistryServiceTest {

    @Test
    public void processEntity(){
        @SomniaEntity(name = "sample_entity", method = StorageMethod.HIT, indexes = {"name", "unknown"})
        class SampleSomniaEntity implements Serializable {
            private String name;
            @IgnoreField
            private String ignoredField;
        }

        EntityRegistryService service = new EntityRegistryService();
        EntityIdentity entityIdentity = service.processEntity(new SampleSomniaEntity());
        assertEquals(entityIdentity.getName(), "sample_entity");
        assertEquals(entityIdentity.getMethod(), StorageMethod.HIT);
        assertEquals(entityIdentity.getFields().size(), 1);
        assertEquals(entityIdentity.getFields().get(0).getName(), "name");
        assertTrue(entityIdentity.getFields().get(0).isIndex());

    }

    @Test
    public void getFingerprint(){
        @SomniaEntity(name = "sample_entity", method = StorageMethod.HIT)
        @EqualsAndHashCode
        class SampleSomniaEntity implements Serializable {
            private String name;
            @IgnoreField
            private String ignoredField;
        }

        EntityRegistryService service = new EntityRegistryService();
        service.register(new SampleSomniaEntity());
        String fingerprint = service.getFingerprint();
        assertNotNull(fingerprint);

        // duplicate registration should be avoided
        service.register(new SampleSomniaEntity());

        assertEquals(service.getFingerprint(), fingerprint);
    }

    @Test
    public void getFingerprintComparison(){
        @SomniaEntity(name = "sample_entity", method = StorageMethod.HIT)
        class SampleSomniaEntity implements Serializable {
            private String name;
        }

        @SomniaEntity(name = "sample_entity", method = StorageMethod.HIT)
        class SampleSomniaEntity2 implements Serializable {
            private String name;
        }

        @SomniaEntity(name = "sample_entity_2", method = StorageMethod.BROADCAST)
        class SampleSomniaEntity3 implements Serializable {
            private String name;
        }

        @SomniaEntity(name = "sample_entity_2", method = StorageMethod.BROADCAST)
        class SampleSomniaEntity4 implements Serializable {
            private String name;
        }

        @SomniaEntity(name = "sample_entity", method = StorageMethod.POPULATE)
        class SampleSomniaEntity5 implements Serializable {
            private String name;
        }

        @SomniaEntity(name = "sample_entity_2", method = StorageMethod.BROADCAST, indexes = {"name"})
        class SampleSomniaEntity6 implements Serializable {
            private String name;
        }

        EntityRegistryService service = new EntityRegistryService();
        service.register(new SampleSomniaEntity());
        String fingerprint1 = service.getFingerprint();
        assertNotNull(fingerprint1);

        // Fingerprint should not change since both entities have same structure and identity
        EntityRegistryService service2 = new EntityRegistryService();
        service2.register(new SampleSomniaEntity2());
        assertEquals(service2.getFingerprint(), fingerprint1);

        // Fingerprints should be different since identities are different
        EntityRegistryService service3 = new EntityRegistryService();
        service3.register(new SampleSomniaEntity3());
        assertNotEquals(service2.getFingerprint(), service3.getFingerprint());

        //multiple registration with same identity should have same fingerprint
        EntityRegistryService service4 = new EntityRegistryService();
        service4.register(new SampleSomniaEntity());
        service4.register(new SampleSomniaEntity3());

        EntityRegistryService service5 = new EntityRegistryService();
        service5.register(new SampleSomniaEntity2());
        service5.register(new SampleSomniaEntity4());

        assertEquals(service4.getFingerprint(), service5.getFingerprint());


        //todo: add more!
    }

}