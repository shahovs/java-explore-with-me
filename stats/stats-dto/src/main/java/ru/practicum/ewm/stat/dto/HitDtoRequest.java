package ru.practicum.ewm.stat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class HitDtoRequest {
    private Long id;
    private String app;
    private String uri;
    private String ip;
    private String timestamp;
}
