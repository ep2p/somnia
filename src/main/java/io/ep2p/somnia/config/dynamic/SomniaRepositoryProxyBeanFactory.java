package io.ep2p.somnia.config.dynamic;

import io.ep2p.somnia.service.SomniaRepositoryEnhancerFactory;

public class SomniaRepositoryProxyBeanFactory {
    private final SomniaRepositoryEnhancerFactory somniaRepositoryEnhancerFactory;

    public SomniaRepositoryProxyBeanFactory(SomniaRepositoryEnhancerFactory somniaRepositoryEnhancerFactory) {
        this.somniaRepositoryEnhancerFactory = somniaRepositoryEnhancerFactory;
    }

    @SuppressWarnings("unchecked")
    public <PS> PS createSomniaRepositoryProxyBean(ClassLoader classLoader, Class<PS> clazz) {
        return this.somniaRepositoryEnhancerFactory.create(classLoader, clazz);
    }
}
