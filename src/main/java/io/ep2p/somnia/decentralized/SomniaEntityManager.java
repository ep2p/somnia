package io.ep2p.somnia.decentralized;

import io.ep2p.somnia.annotation.SomniaDocument;

import java.util.Optional;

public interface SomniaEntityManager {

    Optional<SomniaDocument> getDocumentOfName(String name);

}
