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
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.CompilationNewDto;
import ru.practicum.mainservice.service.impl.CompilationServiceAdminImpl;

@RestController
@RequestMapping(path = "/admin/compilations")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CompilationControllerAdmin {

    private final CompilationServiceAdminImpl compilationServiceAdmin;

    @PostMapping
    ResponseEntity<CompilationDto> saveCompilation(
            @Validated({Create.class}) @RequestBody CompilationNewDto compilationNewDto) {
        log.info("Получен запрос к эндпоинту: POST /admin/compilations, " +
                "Создан объект из тела запроса:'{}'", compilationNewDto);
        CompilationDto result = compilationServiceAdmin.saveCompilation(compilationNewDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

}
