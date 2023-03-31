package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.model.Location;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class EventNewDto {

    @NotNull(message = "title can't be null", groups = {Create.class})
    @Size(min = 3, max = 120, message = "title must have from 3 to 120 characters", groups = {Create.class})
    private String title;

    @NotNull(message = "annotation can't be null", groups = {Create.class})
    @Size(min = 20, max = 2000, message = "annotation must have from 20 to 2000 characters", groups = {Create.class})
    private String annotation;

    @NotNull(message = "description can't be null", groups = {Create.class})
    @Size(min = 20, max = 7000, message = "description must have from 20 to 7000 characters", groups = {Create.class})
    private String description;

    @NotNull(message = "category can't be null", groups = {Create.class})
    private Long category;

    @NotNull(message = "eventDate can't be null", groups = {Create.class})
    // Можно ли как-то задать статус ответа (conflict вместо bad request)? Иначе тесты не проходят.
    // А если в классе ErrorHandler задать conflict, то не пройдут другие тесты - на некорректный body
    // (они ждут, наоборот, bad request)
//    @Future(message = "eventDate must be in future (min 2 hours from now)", groups = {Create.class})
    private LocalDateTime eventDate;

    @NotNull(message = "location can't be null", groups = {Create.class})
    private Location location;

    private Boolean paid;

    @PositiveOrZero(message = "participantLimit must be positive or zero (or null)", groups = {Create.class})
    private Integer participantLimit;

    private Boolean requestModeration;

}
