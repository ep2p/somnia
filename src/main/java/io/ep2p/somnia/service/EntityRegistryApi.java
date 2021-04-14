package io.ep2p.somnia.service;

public interface EntityRegistryApi {
    /**
     * @param o register a SomniaEntity
     */
    void register(Object o);

    /**
     * @return fingerprint of database
     */
    String getFingerprint();

    abstract class Decorator implements EntityRegistryApi {
        private final EntityRegistryApi entityRegistryApi;

        public Decorator(EntityRegistryApi entityRegistryApi) {
            this.entityRegistryApi = entityRegistryApi;
        }

        @Override
        public void register(Object o) {
            this.entityRegistryApi.register(o);
        }

        @Override
        public String getFingerprint() {
            return this.entityRegistryApi.getFingerprint();
        }
    }

}