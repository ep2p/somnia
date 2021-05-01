package io.ep2p.somnia.decentralized;

import com.github.ep2p.kademlia.node.KademliaRepository;
import io.ep2p.somnia.annotation.SomniaDocument;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import io.ep2p.somnia.storage.InMemoryStorage;
import io.ep2p.somnia.storage.MongoStorage;

import java.util.ArrayList;
import java.util.List;

public class SomniaKademliaRepository implements KademliaRepository<SomniaKey, SomniaValue> {
    private final SomniaEntityManager somniaEntityManager;
    private final InMemoryStorage inMemoryStorage;
    private final MongoStorage mongoStorage;

    public SomniaKademliaRepository(SomniaEntityManager somniaEntityManager, InMemoryStorage inMemoryStorage, MongoStorage mongoStorage) {
        this.somniaEntityManager = somniaEntityManager;
        this.inMemoryStorage = inMemoryStorage;
        this.mongoStorage = mongoStorage;
    }



    @Override
    public void store(SomniaKey somniaKey, SomniaValue somniaValue) {
        SomniaDocument somniaDocument = somniaEntityManager.getDocumentOfName(somniaKey.getName()).get();
        if (somniaDocument.inMemory()) {
            inMemoryStorage.store(somniaKey.getKey(), somniaValue.getData());
        }else {
            mongoStorage.store(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaDocument.uniqueKey(), somniaKey, somniaValue);
        }
    }

    @Override
    public SomniaValue get(SomniaKey somniaKey) {
        SomniaDocument somniaDocument = somniaEntityManager.getDocumentOfName(somniaKey.getName()).get();
        if (somniaDocument.inMemory()){
            return inMemoryStorage.get(somniaKey.getKey());
        } else {
            return mongoStorage.get(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaKey);
        }
    }

    @Override
    public void remove(SomniaKey somniaKey) {
        throw new RuntimeException(""); //todo: support multilingual
    }

    @Override
    public boolean contains(SomniaKey somniaKey) {
        SomniaDocument somniaDocument = somniaEntityManager.getDocumentOfName(somniaKey.getName()).get();
        if (somniaDocument.inMemory()){
            return inMemoryStorage.contains(somniaKey.getKey());
        } else {
            return mongoStorage.contains(somniaEntityManager.getClassOfName(somniaKey.getName()), somniaKey);
        }
    }

    @Override
    public List<SomniaKey> getKeys() {
        return new ArrayList<>();
    }
}
