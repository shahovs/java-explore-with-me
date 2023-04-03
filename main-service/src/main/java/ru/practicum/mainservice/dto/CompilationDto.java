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
public class CompilationDto {

    @NotNull(message = "id can't be null", groups = {Create.class})
    private Long id;

    @NotNull(message = "title can't be null", groups = {Create.class})
    private String title;

    @NotNull(message = "pinned can't be null", groups = {Create.class})
    private Boolean pinned;

    private List<EventShortDto> events;

}
