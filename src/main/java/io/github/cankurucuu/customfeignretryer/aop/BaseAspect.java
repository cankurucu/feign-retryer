package io.github.cankurucuu.customfeignretryer.aop;

import io.github.cankurucuu.customfeignretryer.annotation.FeignRetry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class BaseAspect {

    protected Method getMethod(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod();
    }

    protected BackOffPolicy backOffPolicy(FeignRetry feignRetry) {
        if (!Objects.equals(feignRetry.backoff().multiplier(), 0)) {
            ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
            exponentialBackOffPolicy.setInitialInterval(feignRetry.backoff().delay());
            exponentialBackOffPolicy.setMaxInterval(feignRetry.backoff().maxDelay());
            exponentialBackOffPolicy.setMultiplier(feignRetry.backoff().multiplier());
            return exponentialBackOffPolicy;
        } else {
            FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
            fixedBackOffPolicy.setBackOffPeriod(feignRetry.backoff().delay());
            return fixedBackOffPolicy;
        }
    }

    protected SimpleRetryPolicy simpleRetryPolicy(FeignRetry feignRetry) {
        Map<Class<? extends Throwable>, Boolean> policyMap = Arrays.stream(feignRetry.include())
                .map(clazz -> Map.entry(clazz, true))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new SimpleRetryPolicy(feignRetry.maxAttempt(), policyMap, true);
    }

    protected Object proceed(ProceedingJoinPoint proceedingJoinPoint) {
        try {
            return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
