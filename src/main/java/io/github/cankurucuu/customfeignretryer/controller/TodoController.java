package io.github.cankurucuu.customfeignretryer.controller;

import io.github.cankurucuu.customfeignretryer.dto.response.TodoResponse;
import io.github.cankurucuu.customfeignretryer.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping("/{id}")
    public TodoResponse get(@PathVariable Long id) {
        return todoService.get(id);
    }
}
