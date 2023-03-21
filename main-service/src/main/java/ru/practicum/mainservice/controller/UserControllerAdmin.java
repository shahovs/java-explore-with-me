package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.dto.UserDto;
import ru.practicum.mainservice.service.impl.UserServiceAdminImpl;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserControllerAdmin {

    private final UserServiceAdminImpl userService;

    @GetMapping
    ResponseEntity<List<UserDto>> getAllUsersByIds(@RequestParam(name = "ids", required = false) List<Long> userIds,
                                    @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer fromElement,
                                    @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("\n\nПолучен запрос к эндпоинту: GET /admin/users\nuserIds={}, from={}, size={}",
                userIds, fromElement, size);
        List<UserDto> result = userService.getAllUsersByIds(userIds, fromElement, size);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity<UserDto> saveUser(@Validated({Create.class}) @RequestBody UserDto userDto) {
        log.info("\n\nПолучен запрос к эндпоинту: POST /admin/users\nСоздан объект из тела запроса:\n'{}'", userDto);
        UserDto result = userService.saveUser(userDto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{userId}")
    ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("\n\nПолучен запрос к эндпоинту: DELETE /admin/users/{}", userId);
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
