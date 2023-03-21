package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.CompilationNewDto;
import ru.practicum.mainservice.service.impl.CompilationServiceAdminImpl;
import ru.practicum.mainservice.service.impl.CompilationServicePublicImpl;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CompilationControllerPublic {

    private final CompilationServicePublicImpl compilationServicePublic;

    @GetMapping
    ResponseEntity<List<CompilationDto>> getAllCompilations(@RequestParam(defaultValue = "false") Boolean pinned,
                                                            @PositiveOrZero @RequestParam(name = "from",
                                                                    defaultValue = "0") Integer fromElement,
                                                            @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("\n\nПолучен запрос к эндпоинту: GET /compilations, pinned={}, from={}, size={}",
                pinned, fromElement, size);
        List<CompilationDto> result = compilationServicePublic.getAllCompilations(pinned, fromElement, size);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path = "/{compId}")
    ResponseEntity<CompilationDto> getCompilationById(@PathVariable Long compId) {
        log.info("\n\nПолучен запрос к эндпоинту: GET /compilations/{}", compId);
        CompilationDto result = compilationServicePublic.getCompilationById(compId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
