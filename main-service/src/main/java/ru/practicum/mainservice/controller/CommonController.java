package ru.practicum.mainservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CommonController {
    // todo удалить класс
    @GetMapping
    ResponseEntity<?> pathError(HttpServletRequest request) {
        log.info("\n\nОШИБКА Получен запрос к несуществующему эндпоинту: GET {}", request.getPathInfo());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

//    @PostMapping
//    ResponseEntity<UserDto> saveUser(@Validated({Create.class}) @RequestBody UserDto userDto) {
//        log.info("\n\nПолучен запрос к эндпоинту: POST /admin/users, \nСоздан объект из тела запроса:\n'{}'", userDto);
//        UserDto result = userService.saveUser(userDto);
//        return new ResponseEntity<>(result, HttpStatus.CREATED);
//    }
//
//    @DeleteMapping(path = "/{userId}")
//    ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
//        log.info("\n\nПолучен запрос к эндпоинту: DELETE /admin/users/{userId}, userId = {}", userId);
//        userService.deleteUser(userId);
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }

}
