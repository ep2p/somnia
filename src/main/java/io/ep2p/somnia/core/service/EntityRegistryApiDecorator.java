package io.ep2p.somnia.core.service;

public class EntityRegistryApiDecorator implements EntityRegistryApi {
    private final EntityRegistryApi entityRegistryApi;

    public EntityRegistryApiDecorator(EntityRegistryApi entityRegistryApi) {
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
