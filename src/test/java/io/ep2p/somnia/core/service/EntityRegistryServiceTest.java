package io.ep2p.somnia.core.service;

import io.ep2p.somnia.core.annotation.SomniaEntity;
import io.ep2p.somnia.core.model.StorageMethod;
import org.junit.jupiter.api.Test;

class EntityRegistryServiceTest {

    @Test
    public void getFingerprint(){
        @SomniaEntity(name = "sample_entity", method = StorageMethod.HIT)
        class SampleSomniaEntity {
            private String name;
        }

        @SomniaEntity(name = "sample_entity", method = StorageMethod.HIT)
        class SampleSomniaEntity2 {
            private String name;
        }

        @SomniaEntity(name = "sample_entity_2", method = StorageMethod.BROADCAST)
        class SampleSomniaEntity3 {
            private String name;
        }

        @SomniaEntity(name = "sample_entity_2", method = StorageMethod.BROADCAST)
        class SampleSomniaEntity4 {
            private String name;
        }

        @SomniaEntity(name = "sample_entity", method = StorageMethod.POPULATED)
        class SampleSomniaEntity5 {
            private String name;
        }

        @SomniaEntity(name = "sample_entity_2", method = StorageMethod.BROADCAST, indexes = {"name"})
        class SampleSomniaEntity6 {
            private String name;
        }

        //todo

    }

}