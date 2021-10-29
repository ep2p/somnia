package io.ep2p.somnia.decentralized;

import io.ep2p.kademlia.repository.KademliaRepository;
import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.storage.Storage;
import lombok.SneakyThrows;

public class SomniaKademliaRepository implements KademliaRepository<SomniaKey, SomniaValue> {
    private final SomniaEntityManager somniaEntityManager;
    private final Storage cacheStorage;
    private final Storage databaseStorage;

    public SomniaKademliaRepository(SomniaEntityManager somniaEntityManager, Storage cacheStorage, Storage databaseStorage) {
        this.somniaEntityManager = somniaEntityManager;
        this.cacheStorage = cacheStorage;
        this.databaseStorage = databaseStorage;
    }

    @SneakyThrows
    @Override
    public void store(SomniaKey somniaKey, SomniaValue somniaValue) {
        SomniaDocument somniaDocument = somniaEntityManager.getDocumentOfName(somniaKey.getName()).get();
        if (somniaDocument.inMemory()) {
            cacheStorage.store(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaDocument.uniqueKey(), somniaKey, somniaValue);
        }else {
            databaseStorage.store(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaDocument.uniqueKey(), somniaKey, somniaValue);
        }
    }

    @SneakyThrows
    @Override
    public SomniaValue get(SomniaKey somniaKey) {
        SomniaDocument somniaDocument = somniaEntityManager.getDocumentOfName(somniaKey.getName()).get();
        if (somniaDocument.inMemory()){
            return cacheStorage.get(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaKey);
        } else {
            return databaseStorage.get(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaKey);
        }
    }

    @Override
    public void remove(SomniaKey somniaKey) {
        throw new RuntimeException("You can not remove a key from Somnia Repository. This is a dangerous operation and is not available on Somnia API.");
    }

    @SneakyThrows
    @Override
    public boolean contains(SomniaKey somniaKey) {
        SomniaDocument somniaDocument = somniaEntityManager.getDocumentOfName(somniaKey.getName()).get();
        if (somniaDocument.inMemory()){
            return cacheStorage.contains(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaKey);
        } else {
            return databaseStorage.contains(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaKey);
        }
    }
}
