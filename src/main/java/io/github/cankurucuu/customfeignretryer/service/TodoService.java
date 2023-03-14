package io.github.cankurucuu.customfeignretryer.service;

import io.github.cankurucuu.customfeignretryer.client.JsonPlaceHolderClient;
import io.github.cankurucuu.customfeignretryer.dto.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final JsonPlaceHolderClient jsonPlaceHolderClient;

    public TodoResponse get(Long id) {
        return jsonPlaceHolderClient.todos(id);
    }
}
