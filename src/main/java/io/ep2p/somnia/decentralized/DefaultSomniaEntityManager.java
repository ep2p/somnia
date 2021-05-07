package io.ep2p.somnia.decentralized;

import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.SomniaEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultSomniaEntityManager implements SomniaEntityManager {
    private final Map<String, Class<? extends SomniaEntity>> registry = new HashMap<>();

    @Override
    public void register(Class<? extends SomniaEntity> somniaEntityClass) {
        registry.putIfAbsent(somniaEntityClass.getName(), somniaEntityClass);
    }

    @Override
    public Optional<SomniaDocument> getDocumentOfName(String name) {
        Class<? extends SomniaEntity> somniaEntityClass = registry.get(name);
        if (somniaEntityClass != null){
            return Optional.of(getSomniaDocument(somniaEntityClass));
        }
        return Optional.empty();
    }

    @Override
    public Class<? extends SomniaEntity> getClassOfName(String name) throws ClassNotFoundException {
        if (registry.containsKey(name)){
            return registry.get(name);
        }
        throw new ClassNotFoundException("Could not find SomniaEntity class of name '" + name + "'");
    }

    private SomniaDocument getSomniaDocument(Class<?> aClass){
        SomniaDocument somniaDocument = aClass.getAnnotation(SomniaDocument.class);
        if (somniaDocument == null)
            throw new RuntimeException("SomniaEntity sub-class should be marked as @SomniaDocument");
        return somniaDocument;
    }
}
