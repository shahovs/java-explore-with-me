package ru.practicum.ewm.server.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.mapper.HitMapper;
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
        statsRepository.save(HitMapper.toHit(hitDtoRequest));
    }

    public List<HitShortWithHitsDtoResponse> getHits(String start, String end, String[] uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endTime = LocalDateTime.parse(end, formatter);

        return statsRepository.findAllWithHits(startTime, endTime, uris, unique);

        // старая реализация (работает без ошибок):
/*        List<HitShortWithHitsDtoResponse> resultList;

        if (uris == null && !unique) { // первый случай
            resultList = statsRepository.getAllByRequestTimeStampIn(startTime, endTime);
            return resultList;
        }

        if (uris == null) { // if (uris == null && unique) второй случай
            resultList = statsRepository.getAllByRequestTimeStampInUnique(startTime, endTime);
            return resultList;

        }

        // if (uris != null) третий и четвертый случаи
        List<String> urisList = List.of(uris);

        if (!unique) { // if (uris != null && !unique) // третий случай
            resultList = statsRepository.getAllByRequestTimeStampInAndUris(startTime, endTime, urisList);
            return resultList;
        }

        // if (uris != null && unique) // четвертый случай
        resultList = statsRepository.getAllByRequestTimeStampInAndUrisUnique(startTime, endTime, urisList);
        return resultList;*/
    }

}
