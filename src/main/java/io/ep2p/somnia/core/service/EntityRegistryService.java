package io.ep2p.somnia.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.somnia.core.model.EntityIdentity;
import io.ep2p.somnia.core.model.Scheme;
import io.ep2p.somnia.core.util.Validator;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Registers SomniaEntity objects and generates fingerprint of database
 */
public class EntityRegistryService {
    private final Set<Object> objects;
    private final ObjectMapper objectMapper;

    public EntityRegistryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        objects = new HashSet<>();
    }

    public EntityRegistryService(){
        this(new ObjectMapper());
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
    @SneakyThrows
    public String getFingerprint(){
        List<EntityIdentity> idendities = getIdendities(getSortedObjects());
        Scheme scheme = new Scheme(idendities);
        String schemeJson = objectMapper.writeValueAsString(scheme);

        return null;
    }

    /**
     * @return sorted somnia objects
     */
    public List<Object> getSortedObjects(){

        return new ArrayList<>();
    }


    /**
     * @param sortedObjects SomniaEntity sorted objects
     * @return List of EntityIdentity
     */
    public static List<EntityIdentity> getIdendities(List<Object> sortedObjects){

        return new ArrayList<>();
    }

}
