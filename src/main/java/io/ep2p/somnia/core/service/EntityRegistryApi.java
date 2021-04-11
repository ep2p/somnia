package io.ep2p.somnia.core.service;

public interface EntityRegistryApi {
    /**
     * @param o register a SomniaEntity
     */
    void register(Object o);

    /**
     * @return fingerprint of database
     */
    String getFingerprint();
}
