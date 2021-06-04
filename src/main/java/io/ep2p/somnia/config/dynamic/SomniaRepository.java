package io.ep2p.somnia.config.dynamic;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SomniaRepository {
    String bean() default "";
}
