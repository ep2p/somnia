package io.ep2p.somnia.annotation;

import io.ep2p.somnia.model.EntityType;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SomniaDocument {
    boolean uniqueKey() default false;
    EntityType type() default EntityType.HIT;
    boolean inMemory() default false;
}
