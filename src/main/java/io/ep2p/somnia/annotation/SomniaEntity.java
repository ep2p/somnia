package io.ep2p.somnia.annotation;

import io.ep2p.somnia.model.StorageMethod;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SomniaEntity {
    String[] indexes() default "";
    StorageMethod method();
    String name();
}
