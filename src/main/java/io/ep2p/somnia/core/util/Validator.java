package io.ep2p.somnia.core.util;

import io.ep2p.somnia.core.annotation.SomniaEntity;

import java.io.Serializable;

/**
 * Helper util to validate some inputs
 */
public class Validator {


    /**
     * @param o object
     * @return if passed object is a valid SomniaEntity
     */
    public static boolean isValidSomniaEntity(Object o){
        return o instanceof Serializable && o.getClass().getAnnotationsByType(SomniaEntity.class).length > 0;
    }
}
