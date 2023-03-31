package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.Create;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class EventShortDto {

    private Long id;

    @NotNull(message = "title can't be null", groups = {Create.class})
    private String title;

    @NotNull(message = "annotation can't be null", groups = {Create.class})
    private String annotation;

    @NotNull(message = "category can't be null", groups = {Create.class})
    private CategoryDto category;

    @NotNull(message = "eventDate can't be null", groups = {Create.class})
    private LocalDateTime eventDate;

    @NotNull(message = "paid can't be null", groups = {Create.class})
    private Boolean paid;

    @NotNull(message = "initiator can't be null", groups = {Create.class})
    private UserShortDto initiator;

    private Long confirmedRequests;

    private Long views;

    public EventShortDto() {
    }

}
