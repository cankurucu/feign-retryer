package io.github.cankurucuu.customfeignretryer.aop;

import io.github.cankurucuu.customfeignretryer.annotation.FeignRetry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.function.Supplier;

@Slf4j
@Component
@Aspect
public class FeignRetryAspect extends BaseAspect {

    @Pointcut(value = "@annotation(io.github.cankurucuu.customfeignretryer.annotation.FeignRetry)")
    private void feignRetryPointcut() {
    }

    @Around(value = "feignRetryPointcut()")
    public Object feignRetryAround(ProceedingJoinPoint proceedingJoinPoint) {
        Method method = getMethod(proceedingJoinPoint);
        FeignRetry feignRetry = method.getAnnotation(FeignRetry.class);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(backOffPolicy(feignRetry));
        retryTemplate.setRetryPolicy(simpleRetryPolicy(feignRetry));

        return retryTemplate.execute(arg -> {
            log.info("Sending request method: {}, max attempt: {}, delay: {}, retryCount: {}",
                    method, feignRetry.maxAttempt(), feignRetry.backoff().delay(), arg.getRetryCount());
            return super.proceed(proceedingJoinPoint);
        });
    }

}
