package ru.practicum.ewm.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.server.model.HitShortWithHits;
import ru.practicum.ewm.server.service.impl.StatsService;
import ru.practicum.ewm.stat.dto.HitDtoRequest;
import ru.practicum.ewm.stat.dto.HitDtoResponse;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping(path = "/stats")
    List<HitShortWithHits> getHits(@RequestParam String start,
                                   @RequestParam String end,
                                   @RequestParam(required = false) String[] uris, // или defaultValue = "null" ?
                                   @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("\n\nПолучен запрос к эндпоинту: GET /stats, \nstart: {}, end: {}, uris: {}, unique: {}\n",
                start, end, uris, unique);
        return statsService.getHits(start, end, uris, unique);
    }

    @PostMapping(path = "/hit")
    ResponseEntity<?> createHit(@RequestBody HitDtoRequest hitDtoRequest) {
        log.info("\n\nПолучен запрос к эндпоинту: POST /hit, \nСоздан объект из тела запроса:\n'{}'\n", hitDtoRequest);
        statsService.createHit(hitDtoRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
