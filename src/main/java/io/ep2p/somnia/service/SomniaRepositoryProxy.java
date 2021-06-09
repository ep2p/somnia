package io.ep2p.somnia.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.AbstractInvocationHandler;
import io.ep2p.kademlia.exception.GetException;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.somnia.config.dynamic.DynamicRepository;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;
import io.ep2p.somnia.model.GenericObj;
import io.ep2p.somnia.model.SomniaEntity;
import io.ep2p.somnia.model.SomniaKey;
import io.ep2p.somnia.model.SomniaValue;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SomniaRepositoryProxy extends AbstractInvocationHandler {
    private final SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode;
    private final HashGenerator hashGenerator;
    private final ObjectMapper objectMapper;

    public SomniaRepositoryProxy(SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode, HashGenerator hashGenerator, ObjectMapper objectMapper) {
        this.somniaKademliaSyncRepositoryNode = somniaKademliaSyncRepositoryNode;
        this.hashGenerator = hashGenerator;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        DynamicRepository dynamicRepository = proxy.getClass().getAnnotation(DynamicRepository.class);
        assert proxy instanceof GenericObj;
        switch (method.getName()){
            case "save":
                assert args.length == 2 && args[0] instanceof BigInteger && args[1] instanceof Serializable;
                return save(dynamicRepository, proxy, (BigInteger) args[0], (Serializable) args[1]);
            case "findOne":
                assert args.length == 1 && args[0] instanceof BigInteger;
                return findOne(dynamicRepository, proxy, (BigInteger) args[0]);
            case "findAll":
                assert args.length == 1 && args[0] instanceof BigInteger;
                return findAll(dynamicRepository, proxy, (BigInteger) args[0]);
            case "find":
                assert args.length == 4 && args[0] instanceof BigInteger && args[1] instanceof Query
                        && args[2] instanceof Long && args[3] instanceof Integer;
                return find(dynamicRepository, proxy, (BigInteger) args[0], (Query) args[1], (Long) args[2], (Integer) args[3]);
        }
        throw new RuntimeException("Unknown method");
    }

    private Object save(DynamicRepository dynamicRepository, Object proxy, BigInteger key, Serializable data) {
        Class<? extends SomniaEntity<?>> through = dynamicRepository.through();
        SomniaKey somniaKey = SomniaKey.builder()
                .key(key)
                .hash(hashGenerator.hash(key, data))
                .name(through.getName())
                .distributions(0)
                .build();

        SomniaValue somniaValue = SomniaValue.builder()
                .data(objectMapper.valueToTree(data))
                .build();

        try {
            StoreAnswer<BigInteger, SomniaKey> storeAnswer = this.somniaKademliaSyncRepositoryNode.store(somniaKey, somniaValue);
            StoreAnswer.Result result = storeAnswer.getResult();
            if (result == StoreAnswer.Result.FAILED || result == StoreAnswer.Result.TIMEOUT){
                log.error("Failed to store data with key " + somniaKey + " result: " + result);
                return false;
            }
        } catch (StoreException e) {
            log.error("Failed to store data into somnia", e);
            return false;
        }

        return true;
    }

    @SneakyThrows
    private List<?> find(DynamicRepository dynamicRepository, Object proxy, BigInteger key, Query query, long offset, int limit) {
        Class<? extends SomniaEntity<?>> through = dynamicRepository.through();
        SomniaKey somniaKey = SomniaKey.builder()
                .key(key)
                .name(through.getName())
                .meta(SomniaKey.Meta.builder()
                        .query(query.getQueryObject().toJson())
                        .offset(offset)
                        .limit(limit)
                        .build())
                .build();
        return find(somniaKey, proxy);
    }

    @SneakyThrows
    private List<?> findAll(DynamicRepository dynamicRepository, Object proxy, BigInteger key) {
        Class<? extends SomniaEntity<?>> through = dynamicRepository.through();
        SomniaKey somniaKey = SomniaKey.builder()
                .key(key)
                .name(through.getName())
                .build();
        return find(somniaKey, proxy);
    }

    @SneakyThrows
    private Optional<?> findOne(DynamicRepository dynamicRepository, Object proxy, BigInteger key) {
        Class<? extends SomniaEntity<?>> through = dynamicRepository.through();
        SomniaKey somniaKey = SomniaKey.builder()
                .key(key)
                .name(through.getName())
                .meta(SomniaKey.Meta.builder()
                        .offset(0)
                        .limit(1)
                        .build())
                .build();
        List<?> objects = find(somniaKey, proxy);
        return objects.size() > 0 ? Optional.of(objects.get(0)) : Optional.empty();
    }

    private List<?> find(SomniaKey somniaKey, Object proxy) throws JsonProcessingException {
        GetAnswer<BigInteger, SomniaKey, SomniaValue> getAnswer = null;
        try {
            getAnswer = this.somniaKademliaSyncRepositoryNode.get(somniaKey);
            GetAnswer.Result result = getAnswer.getResult();
            if (result == GetAnswer.Result.FAILED || result == GetAnswer.Result.TIMEOUT){
                log.error("Failed to find data with key " + somniaKey + " result: " + result);
                return new ArrayList<>();
            }
        } catch (GetException e) {
            log.error("Failed to find data with key " + somniaKey, e);
        }

        assert getAnswer != null;
        JavaType type = this.objectMapper.getTypeFactory().constructCollectionType(List.class, ((GenericObj) proxy).getGenericClassType(1));
        return this.objectMapper.readValue(getAnswer.getValue().toString(), type);
    }

}
