package io.ep2p.somnia.util;

import io.ep2p.somnia.annotation.SomniaEntity;

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
        return o instanceof Serializable && o.getClass().getAnnotation(SomniaEntity.class) != null;
    }
}
