package io.ep2p.somnia.core.annotation;

import io.ep2p.somnia.core.model.StorageMethod;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SomniaEntity {
    String[] indexes() default "";
    StorageMethod method();
    String name();
}
