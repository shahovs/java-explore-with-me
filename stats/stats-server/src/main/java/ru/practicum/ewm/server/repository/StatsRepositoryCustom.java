package ru.practicum.ewm.server.repository;

import ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepositoryCustom {
    List<HitShortWithHitsDtoResponse> findAllWithHits(LocalDateTime startTime, LocalDateTime endTime,
                                                      List<String> uris, Boolean unique);
}
