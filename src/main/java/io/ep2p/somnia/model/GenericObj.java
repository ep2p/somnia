package io.ep2p.somnia.model;

import java.lang.reflect.ParameterizedType;

@SuppressWarnings("unchecked")
public interface GenericObj {
    default <D> Class<D> getGenericClassType(int i){
        return  ((Class<D>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[i]);
    }
}
