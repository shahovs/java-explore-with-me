package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.dto.UserDto;
import ru.practicum.mainservice.service.impl.CategoryServiceAdminImpl;
import ru.practicum.mainservice.service.impl.CategoryServicePublicImpl;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CategoryControllerPublic {

    private final CategoryServicePublicImpl categoryServicePublic;

    @GetMapping
    ResponseEntity<List<CategoryDto>> getAllCategories(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                       Integer fromElement,
                                                       @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("\n\nПолучен запрос к эндпоинту: GET /categories, from={}, size={}", fromElement, size);
        List<CategoryDto> result = categoryServicePublic.getAllCategories(fromElement, size);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path = "/{catId}")
    ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long catId) {
        log.info("\n\nПолучен запрос к эндпоинту: GET /categories/{catId}, catId={}", catId);
        CategoryDto result = categoryServicePublic.getCategoryById(catId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
