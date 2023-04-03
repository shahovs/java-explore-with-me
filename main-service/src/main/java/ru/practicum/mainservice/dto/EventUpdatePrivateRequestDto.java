package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.Update;
import ru.practicum.mainservice.model.Location;

import javax.validation.constraints.Future;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class EventUpdatePrivateRequestDto {

    @Size(min = 3, max = 120, message = "title must have from 3 to 120 characters", groups = {Update.class})
    private String title;

    @Size(min = 20, max = 2000, message = "annotation must have from 20 to 2000 characters", groups = {Update.class})
    private String annotation;

    @Size(min = 20, max = 7000, message = "description must have from 20 to 7000 characters", groups = {Update.class})
    private String description;

    private Long category;

    @Future(message = "eventDate must be in future (min 2 hours from now)", groups = {Update.class})
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero(message = "participantLimit must be positive or zero (or null)", groups = {Update.class})
    private Integer participantLimit;

    private Boolean requestModeration;

    private EventStateAction stateAction;

}
