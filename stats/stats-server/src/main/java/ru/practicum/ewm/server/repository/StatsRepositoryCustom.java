package ru.practicum.ewm.server.repository;

import ru.practicum.ewm.server.model.HitShortWithHits;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepositoryCustom {
    List<HitShortWithHits> findAllWithHits(LocalDateTime startTime, LocalDateTime endTime, String[] uris);
}
