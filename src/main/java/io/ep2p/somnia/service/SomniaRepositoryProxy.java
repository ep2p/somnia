package io.ep2p.somnia.service;

import com.google.common.reflect.AbstractInvocationHandler;
import io.ep2p.somnia.decentralized.SomniaEntityManager;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;

import java.lang.reflect.Method;

public class SomniaRepositoryProxy extends AbstractInvocationHandler {
    private final SomniaEntityManager somniaEntityManager;
    private final SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode;

    public SomniaRepositoryProxy(SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode, SomniaEntityManager somniaEntityManager) {
        this.somniaKademliaSyncRepositoryNode = somniaKademliaSyncRepositoryNode;
        this.somniaEntityManager = somniaEntityManager;
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(somniaEntityManager.getNames());
        return "";
    }

}
