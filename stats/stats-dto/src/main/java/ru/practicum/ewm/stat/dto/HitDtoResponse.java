package ru.practicum.ewm.stat.dto;

import lombok.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
//@NoArgsConstructor
public class HitDtoResponse {
    private String app;
    private String uri;
    private Long hits;
}
