package io.ep2p.somnia.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ep2p.kademlia.exception.GetException;
import io.ep2p.kademlia.exception.StoreException;
import io.ep2p.kademlia.model.GetAnswer;
import io.ep2p.kademlia.model.StoreAnswer;
import io.ep2p.somnia.config.dynamic.DynamicRepository;
import io.ep2p.somnia.decentralized.SomniaEntityManager;
import io.ep2p.somnia.decentralized.SomniaKademliaSyncRepositoryNode;
import io.ep2p.somnia.model.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SomniaRepositoryEnhancerFactory {
    private final SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode;
    private final HashGenerator hashGenerator;
    private final ObjectMapper objectMapper;
    private final SomniaEntityManager somniaEntityManager;

    public SomniaRepositoryEnhancerFactory(SomniaKademliaSyncRepositoryNode somniaKademliaSyncRepositoryNode, HashGenerator hashGenerator, ObjectMapper objectMapper, SomniaEntityManager somniaEntityManager) {
        this.somniaKademliaSyncRepositoryNode = somniaKademliaSyncRepositoryNode;
        this.hashGenerator = hashGenerator;
        this.objectMapper = objectMapper;
        this.somniaEntityManager = somniaEntityManager;
    }

    public <PS> PS create(ClassLoader classLoader, Class<PS> clazz){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setClassLoader(classLoader);
        enhancer.setCallback(new InvocationHandler() {
            @Override
            public Object invoke(Object object, Method method, Object[] args) throws Throwable {
                Class<?> dataType = null;
                Type[] genericInterfaces = clazz.getGenericInterfaces();
                for (Type genericInterface : genericInterfaces) {
                    if (genericInterface instanceof ParameterizedType) {
                        Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
                        if (genericTypes.length > 0){
                            dataType = Class.forName(genericTypes[0].getTypeName());
                        }
                    }
                }

                DynamicRepository dynamicRepository = clazz.getAnnotation(DynamicRepository.class);
                somniaEntityManager.register(dynamicRepository.through());
                switch (method.getName()){
                    case "save":
                        assert args.length == 2 && args[0] instanceof BigInteger && args[1] instanceof Serializable;
                        return save(dynamicRepository.through(), (BigInteger) args[0], (Serializable) args[1]);
                    case "findOne":
                        assert args.length == 1 && args[0] instanceof BigInteger;
                        return findOne(dynamicRepository.through(), dataType, (BigInteger) args[0]);
                    case "findAll":
                        assert args.length == 1 && args[0] instanceof BigInteger;
                        return findAll(dynamicRepository.through(), dataType, (BigInteger) args[0]);
                    case "find":
                        if (args.length == 3 && args[0] instanceof BigInteger
                                && args[1] instanceof Long && args[2] instanceof Integer){
                            return find(dynamicRepository.through(), dataType, (BigInteger) args[0], (Long) args[1], (Integer) args[2]);
                        }
                        if (args.length == 4 && args[0] instanceof BigInteger && args[1] instanceof Query
                                && args[2] instanceof Long && args[3] instanceof Integer){
                            return find(dynamicRepository.through(), dataType, (BigInteger) args[0], (Query) args[1], (Long) args[2], (Integer) args[3]);
                        }
                }
                throw new RuntimeException("Unknown method");
            }
        });
        return (PS) enhancer.create();
    }


    private RepositoryResponse<?> save(Class<?> through, BigInteger key, Serializable data) {
        SomniaKey somniaKey = SomniaKey.builder()
                .key(key)
                .hash(hashGenerator.hash(key, data))
                .name(through.getName())
                .distributions(0)
                .build();

        SomniaValue somniaValue = SomniaValue.builder()
                .data(objectMapper.valueToTree(data))
                .build();

        StoreAnswer<BigInteger, SomniaKey> storeAnswer = null;
        try {
            storeAnswer = this.somniaKademliaSyncRepositoryNode.store(somniaKey, somniaValue);
            StoreAnswer.Result result = storeAnswer.getResult();
            if (result == StoreAnswer.Result.FAILED || result == StoreAnswer.Result.TIMEOUT){
                log.error("Failed to store data with key " + somniaKey + " result: " + result);
                return RepositoryResponse.builder().build();
            }
        } catch (StoreException e) {
            log.error("Failed to store data into somnia", e);
            return RepositoryResponse.builder().build();
        }

        return RepositoryResponse.builder()
                .success(true)
                .node(storeAnswer.getNodeId())
                .build();
    }

    @SneakyThrows
    private RepositoryResponse<?> find(Class<? extends SomniaEntity<?>> through, Class<?> dataType, BigInteger key, Long offset, Integer limit) {
        SomniaKey somniaKey = SomniaKey.builder()
                .key(key)
                .name(through.getName())
                .meta(SomniaKey.Meta.builder()
                        .offset(offset)
                        .limit(limit)
                        .build())
                .build();
        return find(somniaKey, dataType);
    }

    @SneakyThrows
    private RepositoryResponse<?> find(Class<?> through, Class<?> dataType, BigInteger key, Query query, long offset, int limit) {
        SomniaKey somniaKey = SomniaKey.builder()
                .key(key)
                .name(through.getName())
                .meta(SomniaKey.Meta.builder()
                        .query(query.getQueryObject().toJson())
                        .offset(offset)
                        .limit(limit)
                        .build())
                .build();
        return find(somniaKey, dataType);
    }

    @SneakyThrows
    private RepositoryResponse<?> findAll(Class<?> through, Class<?> dataType, BigInteger key) {
        SomniaKey somniaKey = SomniaKey.builder()
                .key(key)
                .name(through.getName())
                .build();
        return find(somniaKey, dataType);
    }

    @SneakyThrows
    private <E> RepositoryResponse<E> findOne(Class<?> through, Class<?> dataType, BigInteger key) {
        SomniaKey somniaKey = SomniaKey.builder()
                .key(key)
                .hash(key)
                .name(through.getName())
                .meta(SomniaKey.Meta.builder()
                        .offset(0)
                        .limit(1)
                        .build())
                .build();
        RepositoryResponse<E> repositoryResponse = (RepositoryResponse<E>) find(somniaKey, dataType);
        if (repositoryResponse.isSuccess()) {
            repositoryResponse.setResult(repositoryResponse.getResults().size() > 0 ? repositoryResponse.getResults().get(0): null);
            repositoryResponse.setResults(null);
        }
        return repositoryResponse;
    }

    private RepositoryResponse<?> find(SomniaKey somniaKey, Class<?> dataType) throws JsonProcessingException {
        GetAnswer<BigInteger, SomniaKey, SomniaValue> getAnswer = null;
        try {
            getAnswer = this.somniaKademliaSyncRepositoryNode.get(somniaKey, 30, TimeUnit.SECONDS);
            GetAnswer.Result result = getAnswer.getResult();
            if (result == GetAnswer.Result.FAILED || result == GetAnswer.Result.TIMEOUT){
                log.error("Failed to find data with key " + somniaKey + " result: " + result);
                return RepositoryResponse.builder().build();
            }
        } catch (GetException e) {
            log.error("Failed to find data with key " + somniaKey, e);
        }

        assert getAnswer != null;
        JavaType type = this.objectMapper.getTypeFactory().constructCollectionType(List.class, dataType);
        return RepositoryResponse.builder()
                .node(getAnswer.getNodeId())
                .results(this.objectMapper.readValue(getAnswer.getValue().getData().toString(), type))
                .success(true)
                .build();
    }

}
