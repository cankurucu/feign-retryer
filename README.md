# Custom Feign Retryer

### Prerequisites

List the software and tools needed to run the project in this section.

- Java 11
- Maven
- Spring Boot

### Installation

Step by step, explain how to set up the project on a local machine.

1. Clone the project
```bash
git clone git@github.com:cankurucu/feign-retryer.git
```
2. Compile the project with Maven
```bash
mvn clean install
```
3. Run the Spring Boot application
```bash
mvn spring-boot:run
```

## Usage

### Custom Annotations
#### io.github.cankurucuu.customfeignretryer.annotation.Backoff

```
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
```

The io.github.cankurucuu.customfeignretryer.annotation.Backoff is a Java annotation. This annotation is used to define the retry policy for Feign clients' HTTP requests.  The parameters inside the annotation are as follows:  
- `delay()`: This parameter specifies how long to wait before the first retry attempt. The default value is 1000 milliseconds.
- `maxDelay()`: This parameter specifies the maximum delay between retry attempts. The default value is 0 milliseconds, which means there is no maximum delay.
- `multiplier()`: This parameter specifies how much the delay increases after each retry attempt. The default value is 0.0, which means the delay does not increase.
This annotation is used on a Feign client class or method. The parameters specified by this annotation determine how long to wait and retry the HTTP request if it fails. This is useful in situations like network errors or temporary server errors. This annotation makes Feign clients more resilient in error situations.

#### io.github.cankurucuu.customfeignretryer.annotation.FeignRetry

```
package io.github.cankurucuu.customfeignretryer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FeignRetry {

    Backoff backoff() default @Backoff();

    int maxAttempt() default 3;

    Class<? extends Throwable>[] include() default {};
}
```

The `io.github.cankurucuu.customfeignretryer.annotation.FeignRetry` is a Java annotation used to define the retry policy for Feign clients' HTTP requests. This annotation can be applied to a Feign client class or method.

Here are the parameters inside the `FeignRetry` annotation:

- `backoff()`: This parameter is an instance of the `Backoff` annotation. It specifies the backoff policy to use between retry attempts. The `Backoff` annotation includes parameters for delay, maxDelay, and multiplier, which define the initial delay, maximum delay, and the factor by which the delay should increase after each attempt, respectively.

- `maxAttempt()`: This parameter specifies the maximum number of retry attempts. The default value is 3.

- `include()`: This parameter is an array of Throwable classes. If an exception is thrown that is an instance of any of the specified classes, a retry will be attempted. By default, this array is empty, which means no exceptions will trigger a retry.

In conclusion, the `FeignRetry` annotation allows you to specify a retry policy for a Feign client. This can make your Feign clients more resilient to temporary network or server errors.

### Aspects
#### io.github.cankurucuu.customfeignretryer.aop.BaseAspect

```
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
```

The `io.github.cankurucuu.customfeignretryer.aop.BaseAspect` is an abstract class that provides base functionality for Aspect Oriented Programming (AOP) in the context of Feign clients with retry policies.

Here's a breakdown of its methods:

- `getMethod(JoinPoint joinPoint)`: This method retrieves the `Method` instance associated with the join point (the point in the program execution at which an aspect's advice is invoked).

- `backOffPolicy(FeignRetry feignRetry)`: This method creates a `BackOffPolicy` based on the `FeignRetry` annotation. If the multiplier in the `FeignRetry` annotation is not zero, it creates an `ExponentialBackOffPolicy` with the specified initial interval, maximum interval, and multiplier. Otherwise, it creates a `FixedBackOffPolicy` with the specified backoff period.

- `simpleRetryPolicy(FeignRetry feignRetry)`: This method creates a `SimpleRetryPolicy` based on the `FeignRetry` annotation. It creates a map of exceptions that should trigger a retry and sets this map in the `SimpleRetryPolicy`. The maximum number of retry attempts is also set from the `FeignRetry` annotation.

- `proceed(ProceedingJoinPoint proceedingJoinPoint)`: This method proceeds with the method execution at the join point. If an exception is thrown during the execution, it is caught and wrapped in a `RuntimeException`.

This class is used as a base for creating aspects that apply retry policies to Feign clients. The aspects created from this base class can intercept calls to methods annotated with `FeignRetry` and apply the retry policy specified in the annotation.

#### io.github.cankurucuu.customfeignretryer.aop.FeignRetryAspect

```
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
```

The `io.github.cankurucuu.customfeignretryer.aop.FeignRetryAspect` is a concrete implementation of the `BaseAspect` abstract class. It uses Aspect Oriented Programming (AOP) to apply retry policies to Feign clients.

This class defines an aspect that intercepts calls to methods annotated with `FeignRetry`. When a method with this annotation is invoked, the aspect applies the retry policy specified in the annotation.

Here's a breakdown of its methods:

- `@Around("@annotation(feignRetry)"): This is an advice that gets executed around (both before and after) the method execution. The `@Around` annotation takes a pointcut expression as a parameter. In this case, it's `@annotation(feignRetry)`, which matches any method execution where the method has the `FeignRetry` annotation.

- `executeWithRetry(ProceedingJoinPoint proceedingJoinPoint, FeignRetry feignRetry)`: This method is the implementation of the advice. It creates a `RetryTemplate` with the retry policy and backoff policy specified in the `FeignRetry` annotation, and then executes the method at the join point with this `RetryTemplate`. If the method execution throws an exception that matches the exceptions specified in the `FeignRetry` annotation, the `RetryTemplate` will retry the method execution according to the retry policy.

In conclusion, the `FeignRetryAspect` class is an aspect that makes Feign clients more resilient by applying retry policies to their HTTP requests.

### Clients
#### io.github.cankurucuu.customfeignretryer.client.JsonPlaceHolderClient

```
package io.github.cankurucuu.customfeignretryer.client;

import feign.RetryableException;
import io.github.cankurucuu.customfeignretryer.annotation.Backoff;
import io.github.cankurucuu.customfeignretryer.annotation.FeignRetry;
import io.github.cankurucuu.customfeignretryer.dto.response.TodoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "json-place-holder-client", url = "https://jsonplaceholder.typicode.com")
public interface JsonPlaceHolderClient {

    @GetMapping("/todos/{id}")
    @FeignRetry(backoff = @Backoff(delay = 500L, maxDelay = 20000L, multiplier = 4L), maxAttempt = 5, include = {RetryableException.class})
    TodoResponse todos(@PathVariable Long id);
}

```

The `io.github.cankurucuu.customfeignretryer.client.JsonPlaceHolderClient.todos` method is a part of the `JsonPlaceHolderClient` interface, which is a Feign client. This method is used to send a GET request to the `https://jsonplaceholder.typicode.com/todos/{id}` endpoint to retrieve a "todo" item with a specific ID.

The `@FeignClient(name = "json-place-holder-client", url = "https://jsonplaceholder.typicode.com")` annotation at the class level specifies that this client will send requests to the `https://jsonplaceholder.typicode.com` base URL.

The `@GetMapping("/todos/{id}")` annotation at the method level indicates that this method sends a GET request to the `/todos/{id}` path. The `{id}` path variable is replaced with the `Long id` parameter of the method.

The `@FeignRetry` annotation is used to apply a retry policy to this method. The parameters of this annotation specify the details of the retry policy. In this case, `maxAttempt = 5` specifies the maximum number of retry attempts, `delay = 500L`, `maxDelay = 20000L` and `multiplier = 4L` specify the backoff strategy, and `include = {RetryableException.class}` specifies that a retry will be attempted if a `RetryableException` is thrown.

In conclusion, the `todos` method sends a GET request to the `https://jsonplaceholder.typicode.com/todos/{id}` endpoint to retrieve a "todo" item with a specific ID, and it will retry the request according to the specified retry policy if a `RetryableException` is thrown.
