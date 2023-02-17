package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.service.impl.CategoryServiceAdminImpl;

@RestController
@RequestMapping(path = "/admin/categories")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CategoryControllerAdmin {

    private final CategoryServiceAdminImpl categoryService;

    @PostMapping
    ResponseEntity<CategoryDto> saveCategory(@Validated({Create.class}) @RequestBody CategoryDto categoryDto) {
        log.info("Получен запрос к эндпоинту: POST /admin/categories, Создан объект из тела запроса:'{}'", categoryDto);
        CategoryDto result = categoryService.saveCategory(categoryDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

}
