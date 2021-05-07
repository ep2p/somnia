package io.ep2p.somnia.decentralized;

import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.SomniaEntity;

import java.util.Optional;

public interface SomniaEntityManager {

    void register(Class<? extends SomniaEntity> somniaEntityClass);
    Optional<SomniaDocument> getDocumentOfName(String name);
    Class<? extends SomniaEntity> getClassOfName(String name) throws ClassNotFoundException;

}
