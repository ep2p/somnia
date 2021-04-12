package io.ep2p.somnia.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import io.ep2p.somnia.core.annotation.IgnoreField;
import io.ep2p.somnia.core.annotation.SomniaEntity;
import io.ep2p.somnia.core.model.EntityIdentity;
import io.ep2p.somnia.core.model.Scheme;
import io.ep2p.somnia.core.util.Validator;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Registers SomniaEntity objects and generates fingerprint of database
 */
@AllArgsConstructor
public class EntityRegistryService implements EntityRegistryApi {
    private final List<Object> objects = new ArrayList<>();
    private final ObjectMapper objectMapper;
    private final FingerprintApi fingerprintApi;

    public EntityRegistryService(){
        this(new ObjectMapper(), new FingerprintApi.DefaultFingerprintApi());
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

        return fingerprintApi.generateFingerprint(schemeJson);
    }

    public void sortObjects(){
        this.objects.sort(new Comparator<Object>() {
            @Override
            public int compare(final Object object1, final Object object2) {
                String o1name = object1.getClass().getAnnotationsByType(SomniaEntity.class)[0].name();
                String o2name = object2.getClass().getAnnotationsByType(SomniaEntity.class)[0].name();
                return o1name.compareTo(o2name);
            }
        });
    }


    /**
     * @return List of EntityIdentity
     */
    public List<EntityIdentity> getIdentities(){
        List<EntityIdentity> identities = new ArrayList<>();
        Set<String> names = new HashSet<>();
        this.objects.forEach(o -> {
            EntityIdentity entityIdentity = processEntity(o);
            if (!names.contains(entityIdentity.getName())){
                identities.add(entityIdentity);
                names.add(entityIdentity.getName());
            }
        });

        return identities;
    }

    @VisibleForTesting
    private EntityIdentity processEntity(Object object) {
        SomniaEntity somniaEntity = object.getClass().getAnnotationsByType(SomniaEntity.class)[0];
        return EntityIdentity.builder()
                .method(somniaEntity.method())
                .name(somniaEntity.name())
                .fields(processFields(object, Arrays.asList(somniaEntity.indexes())))
                .build();
    }

    @VisibleForTesting
    private List<EntityIdentity.FieldIdentity> processFields(Object object, List<String> indexes){
        List<EntityIdentity.FieldIdentity> fieldIdentities = new ArrayList<>();

        for (Field declaredField : object.getClass().getDeclaredFields()) {
            if (declaredField.getAnnotationsByType(IgnoreField.class).length > 0 || declaredField.getName().equals("this$0"))
                continue;
            fieldIdentities.add(EntityIdentity.FieldIdentity.builder()
                    .index(indexes.contains(declaredField.getName()))
                    .type(declaredField.getType().getName())
                    .name(declaredField.getName())
                    .build());

        }

        return fieldIdentities;
    }

}
