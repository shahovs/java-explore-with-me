package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CompilationUpdateRequestDto {
    private String title;
    private Boolean pinned;
    private List<Long> events;
}
