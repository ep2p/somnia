package io.ep2p.somnia.model;

import java.lang.reflect.ParameterizedType;

public interface GenericObj {
    default <D> Class<D> getListenerMessageBodyClassType(int i){
        return  ((Class<D>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[i]);
    }
}
