package io.ep2p.somnia.storage;

import io.ep2p.somnia.model.SomniaEntity;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;

public interface Storage {
    void store(Class<? extends SomniaEntity<?>> classOfName, boolean uniqueKey, SomniaKey somniaKey, SomniaValue somniaValue);
    SomniaValue get(Class<? extends SomniaEntity<?>> classOfName, SomniaKey somniaKey);
    boolean contains(Class<? extends SomniaEntity<?>> classOfName, SomniaKey somniaKey);
}
