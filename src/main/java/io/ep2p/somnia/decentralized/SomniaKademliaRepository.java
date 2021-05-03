package io.ep2p.somnia.decentralized;

import com.github.ep2p.kademlia.node.KademliaRepository;
import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class SomniaKademliaRepository implements KademliaRepository<SomniaKey, SomniaValue> {
    private final SomniaEntityManager somniaEntityManager;
    private final Storage inMemoryStorage;
    private final Storage mongoStorage;

    public SomniaKademliaRepository(SomniaEntityManager somniaEntityManager, Storage inMemoryStorage, Storage mongoStorage) {
        this.somniaEntityManager = somniaEntityManager;
        this.inMemoryStorage = inMemoryStorage;
        this.mongoStorage = mongoStorage;
    }


    @Override
    public void store(SomniaKey somniaKey, SomniaValue somniaValue) {
        SomniaDocument somniaDocument = somniaEntityManager.getDocumentOfName(somniaKey.getName()).get();
        if (somniaDocument.inMemory()) {
            inMemoryStorage.store(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaDocument.uniqueKey(), somniaKey, somniaValue);
        }else {
            mongoStorage.store(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaDocument.uniqueKey(), somniaKey, somniaValue);
        }
    }

    @Override
    public SomniaValue get(SomniaKey somniaKey) {
        SomniaDocument somniaDocument = somniaEntityManager.getDocumentOfName(somniaKey.getName()).get();
        if (somniaDocument.inMemory()){
            return inMemoryStorage.get(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaKey);
        } else {
            return mongoStorage.get(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaKey);
        }
    }

    @Override
    public void remove(SomniaKey somniaKey) {
        throw new RuntimeException("You can not remove a key from Somnia Repository. This is a dangerous operation and is not available on Somnia API.");
    }

    @Override
    public boolean contains(SomniaKey somniaKey) {
        SomniaDocument somniaDocument = somniaEntityManager.getDocumentOfName(somniaKey.getName()).get();
        if (somniaDocument.inMemory()){
            return inMemoryStorage.contains(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaKey);
        } else {
            return mongoStorage.contains(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaKey);
        }
    }

    @Override
    public List<SomniaKey> getKeys() {
        return new ArrayList<>();
    }
}
