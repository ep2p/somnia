package io.ep2p.somnia.config.dynamic;

import io.ep2p.somnia.service.SomniaRepositoryProxy;

import java.lang.reflect.Proxy;

public class SomniaRepositoryProxyBeanFactory {
    private final SomniaRepositoryProxy somniaRepositoryProxy;

    public SomniaRepositoryProxyBeanFactory(SomniaRepositoryProxy somniaRepositoryProxy) {
        this.somniaRepositoryProxy = somniaRepositoryProxy;
    }

    @SuppressWarnings("unchecked")
    public <PS> PS createSomniaRepositoryProxyBean(ClassLoader classLoader, Class<PS> clazz) {
        return (PS) Proxy.newProxyInstance(classLoader, new Class[] {clazz}, somniaRepositoryProxy);
    }
}
