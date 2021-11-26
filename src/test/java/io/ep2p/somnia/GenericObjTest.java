package io.ep2p.somnia;

import io.ep2p.somnia.spring.mock.SampleData;
import io.ep2p.somnia.spring.mock.SampleSomniaEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class GenericObjTest {

    @Test
    public void getGenericClassType(){
        SampleSomniaEntity sampleSomniaEntity = new SampleSomniaEntity();
        Class<Object> genericClassType = sampleSomniaEntity.getGenericClassType(0);
        Assertions.assertEquals(genericClassType, SampleData.class);
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> sampleSomniaEntity.getGenericClassType(2));
    }
}
