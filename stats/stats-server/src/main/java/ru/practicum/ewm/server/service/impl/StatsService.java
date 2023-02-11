package ru.practicum.ewm.server.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.mapper.HitMapper;
import ru.practicum.ewm.server.model.Hit;
import ru.practicum.ewm.server.repository.StatsRepository;
import ru.practicum.ewm.stat.dto.HitDtoRequest;
import ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatsService {

    private final StatsRepository statsRepository;

    @Transactional
    public void createHit(HitDtoRequest hitDtoRequest) {
        Hit save = statsRepository.save(HitMapper.toHit(hitDtoRequest));
        System.out.println("\n777 Saved hit:\n" + save);
    }

    public List<HitShortWithHitsDtoResponse> getHits(String start, String end, String[] uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endTime = LocalDateTime.parse(end, formatter);

        return statsRepository.findAllWithHits(startTime, endTime, uris, unique);

        //todo убрать из закомментированного кода логи (можно убрать и весь код)
/*
        List<HitShortWithHits> resultList;

        if (uris == null && !unique) {
            System.out.println("\n777 if (uris == null && !unique)\n");
            resultList = statsRepository.getAllByRequestTimeStampIn(startTime, endTime);
            System.out.println("\nПолучено из репозитория:\n" + resultList);
            return resultList;
        }

        if (uris == null) { //uris == null && unique
            System.out.println("\n777 if (uris == null && unique)\n");
            resultList = statsRepository.getAllByRequestTimeStampInUnique(startTime, endTime);
            System.out.println("\nПолучено из репозитория:\n" + resultList);
            return resultList;

        }

        // if uris != null
        List<String> urisList = List.of(uris);

        if (!unique) { //uris != null && !unique
            System.out.println("\n777 if (uris != null && !unique)\n");
            resultList = statsRepository.getAllByRequestTimeStampInAndUris(startTime, endTime, urisList);
            System.out.println("\nПолучено из репозитория:\n" + resultList);
            return resultList;
        }

        //uris != null && unique
        System.out.println("\n777 if (uris != null && unique)\n");
        resultList = statsRepository.getAllByRequestTimeStampInAndUrisUnique(startTime, endTime, urisList);
        System.out.println("\nПолучено из репозитория:\n" + resultList);
        return resultList;
        */
    }

}
