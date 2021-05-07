package io.ep2p.somnia.decentralized;

import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.SomniaEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultSomniaEntityManager implements SomniaEntityManager {
    private final Map<String, SomniaEntity<?>> registry = new HashMap<>();

    @Override
    public void register(SomniaEntity<?> somniaEntity) {
        registry.putIfAbsent(somniaEntity.getClass().getName(), somniaEntity);
    }

    @Override
    public Optional<SomniaDocument> getDocumentOfName(String name) {
        SomniaEntity<?> somniaEntity = registry.get(name);
        if (somniaEntity != null){
            return Optional.of(somniaEntity.getSomniaDocument());
        }
        return Optional.empty();
    }

    @Override
    public Class<? extends SomniaEntity<?>> getClassOfName(String name) throws ClassNotFoundException {
        if (registry.containsKey(name)){
            return (Class<? extends SomniaEntity<?>>) registry.get(name).getClass();
        }
        throw new ClassNotFoundException("Could not find SomniaEntity class of name '" + name + "'");
    }
}
