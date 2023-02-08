package ru.practicum.ewm.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class HitShortWithHits /*implements HitShort*/ {
    private String app;
    private String uri;
    private Long hits;

//    public HitShortWithHits(HitShort hitShort, Long hits){
//        this.app = hitShort.getApp();
//        this.uri = hitShort.getUri();
//        this.hits = hits;
//    }
}
