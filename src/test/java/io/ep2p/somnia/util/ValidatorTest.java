package io.ep2p.somnia.util;

import io.ep2p.somnia.annotation.SomniaEntity;
import io.ep2p.somnia.model.StorageMethod;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @Test
    public void isValidSomniaEntity(){

        @SomniaEntity(name = "sample_entity", method = StorageMethod.HIT)
        class SampleSomniaEntity {}

        assertFalse(Validator.isValidSomniaEntity(new SampleSomniaEntity()));

        class SampleSomniaEntity2 implements Serializable {}

        assertFalse(Validator.isValidSomniaEntity(new SampleSomniaEntity2()));

        @SomniaEntity(name = "sample_entity", method = StorageMethod.HIT)
        class SampleSomniaEntity3 implements Serializable {}

        assertTrue(Validator.isValidSomniaEntity(new SampleSomniaEntity3()));
    }

}