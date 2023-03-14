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
