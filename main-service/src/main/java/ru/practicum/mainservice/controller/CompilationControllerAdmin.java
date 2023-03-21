package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.dto.CompilationDto;
import ru.practicum.mainservice.dto.CompilationNewDto;
import ru.practicum.mainservice.dto.CompilationUpdateRequestDto;
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
        log.info("\n\nПолучен запрос к эндпоинту: POST /admin/compilations, " +
                "\nСоздан объект из тела запроса:\n'{}'", compilationNewDto);
        CompilationDto result = compilationServiceAdmin.saveCompilation(compilationNewDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PatchMapping(path = "/{compId}")
    ResponseEntity<CompilationDto> updateCompilation(
            @PathVariable Long compId,
            @RequestBody CompilationUpdateRequestDto compilationUpdateRequestDto) {
        log.info("\n\nПолучен запрос к эндпоинту: PATCH /admin/compilations/{}" +
                "\nСоздан объект из тела запроса:\n'{}'", compId, compilationUpdateRequestDto);
        CompilationDto result = compilationServiceAdmin.updateCompilation(compId, compilationUpdateRequestDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping(path = "/{compId}")
    ResponseEntity<Void> deleteCompilation(@PathVariable Long compId) {
        log.info("\n\nПолучен запрос к эндпоинту: DELETE /admin/compilations/{}", compId);
        compilationServiceAdmin.deleteCompilation(compId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
