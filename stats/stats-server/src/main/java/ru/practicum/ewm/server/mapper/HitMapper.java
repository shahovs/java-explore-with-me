package ru.practicum.ewm.server.mapper;

import ru.practicum.ewm.server.model.Hit;
import ru.practicum.ewm.stat.dto.HitDtoRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HitMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Hit toHit(HitDtoRequest hitDtoRequest) {
        Hit hit = new Hit();
        hit.setApp(hitDtoRequest.getApp());
        hit.setIp(hitDtoRequest.getIp());
        hit.setUri(hitDtoRequest.getUri());
        hit.setRequestTimeStamp(LocalDateTime.parse(hitDtoRequest.getTimestamp(), formatter));
        return hit;
    }
}
