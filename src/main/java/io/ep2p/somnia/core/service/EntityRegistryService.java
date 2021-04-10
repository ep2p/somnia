package io.ep2p.somnia.core.service;

import io.ep2p.somnia.core.util.Validator;

import java.util.HashSet;
import java.util.Set;

/**
 * Registers SomniaEntity objects and generates fingerprint of database
 */
public class EntityRegistryService {
    private final Set<Object> objects;
    private String cachedFingerPrint;

    public EntityRegistryService() {
        objects = new HashSet<>();
    }

    /**
     * @param o register a SomniaEntity
     */
    public void register(Object o){
        if (Validator.isValidSomniaEntity(o)) {
            objects.add(o);
        }
    }

    //todo

    /**
     * @return fingerprint of database
     */
    public String getFingerprint(){

        return null;
    }

    private boolean isCached(){
        return cachedFingerPrint != null;
    }

}
