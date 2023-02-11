package ru.practicum.ewm.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.server.model.Hit;
import ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Hit, Long>, StatsRepositoryCustom {

    @Query(value = "select new ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse " +
            "(h.app, h.uri, count(*)) " +
            "from Hit h " +
            "where h.requestTimeStamp between :start and :end " +
            "group by h.app, h.uri " +
            "order by count(*) desc ")
    List<HitShortWithHitsDtoResponse> getAllByRequestTimeStampIn(LocalDateTime start, LocalDateTime end);

    @Query(value = "select new ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse " +
            "(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit h " +
            "where h.requestTimeStamp between :start and :end " +
            "group by h.app, h.uri " +
            "order by count(h.ip) desc ")
    List<HitShortWithHitsDtoResponse> getAllByRequestTimeStampInUnique(LocalDateTime start, LocalDateTime end);

    @Query(value = "select new ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse " +
            "(h.app, h.uri, count(*)) " +
            "from Hit h " +
            "where h.requestTimeStamp between :start and :end " +
            "and h.uri in :uris " +
            "group by h.app, h.uri " +
            "order by count(*) desc ")
    List<HitShortWithHitsDtoResponse> getAllByRequestTimeStampInAndUris(LocalDateTime start, LocalDateTime end,
                                                                        List<String> uris);

    @Query(value = "select new ru.practicum.ewm.stat.dto.HitShortWithHitsDtoResponse " +
            "(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit h " +
            "where h.requestTimeStamp between :start and :end " +
            "and h.uri in :uris " +
            "group by h.app, h.uri " +
            "order by count(h.ip) desc ")
    List<HitShortWithHitsDtoResponse> getAllByRequestTimeStampInAndUrisUnique(LocalDateTime start, LocalDateTime end,
                                                                              List<String> uris);

}
