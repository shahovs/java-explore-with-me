package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.Create;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ToString
public class CompilationNewDto {

    @NotNull(message = "title can't be null", groups = {Create.class})
    private String title;

    private Boolean pinned;

    private List<Long> events; // значения должны быть уникальными (возможно, Set будет лучше)

}
