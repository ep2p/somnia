package io.ep2p.somnia.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.somnia.core.model.EntityIdentity;
import io.ep2p.somnia.core.model.Scheme;
import io.ep2p.somnia.core.util.Validator;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers SomniaEntity objects and generates fingerprint of database
 */
public class EntityRegistryService {
    private final List<Object> objects;
    private final ObjectMapper objectMapper;

    public EntityRegistryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        objects = new ArrayList<>();
    }

    public EntityRegistryService(){
        this(new ObjectMapper());
    }

    /**
     * @param o register a SomniaEntity
     */
    public synchronized void register(Object o){
        if (!objects.contains(o) && Validator.isValidSomniaEntity(o)) {
            objects.add(o);
        }
    }

    //todo

    /**
     * @return fingerprint of database
     */
    @SneakyThrows
    public synchronized String getFingerprint(){
        sortObjects();
        List<EntityIdentity> idendities = getIdentities();
        Scheme scheme = new Scheme(idendities);
        String schemeJson = objectMapper.writeValueAsString(scheme);

        return null;
    }

    public void sortObjects(){

    }


    /**
     * @return List of EntityIdentity
     */
    public static List<EntityIdentity> getIdentities(){

        return new ArrayList<>();
    }

}
