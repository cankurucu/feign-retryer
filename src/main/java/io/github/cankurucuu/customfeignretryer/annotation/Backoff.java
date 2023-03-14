package io.github.cankurucuu.customfeignretryer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Backoff {

    long delay() default 1000L;

    long maxDelay() default 0L;

    double multiplier() default 0.0D;
}