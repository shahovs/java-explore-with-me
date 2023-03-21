package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.model.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class EventFullDto {

    private Long id;

    @NotNull(message = "title can't be null", groups = {Create.class})
    private String title;

    @NotNull(message = "annotation can't be null", groups = {Create.class})
    private String annotation;

    private String description;

    @NotNull(message = "category can't be null", groups = {Create.class})
    private CategoryDto category;

    @NotNull(message = "eventDate can't be null", groups = {Create.class})
    private LocalDateTime eventDate;

    @NotNull(message = "location can't be null", groups = {Create.class})
    private Location location;

    @NotNull(message = "paid can't be null", groups = {Create.class})
    private Boolean paid;

    @PositiveOrZero(message = "participantLimit must be positive or zero (or null)", groups = {Create.class})
    private Integer participantLimit;

    private Boolean requestModeration;

    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;

    private UserShortDto initiator;

    private EventState state;

    private Long confirmedRequests;

    private Long views;

    public EventFullDto(
            Long id, String title, String annotation, String description,
            Long categoryId, String categoryName,
            LocalDateTime eventDate, Location location, Boolean paid, Integer participantLimit,
            Boolean requestModeration, java.time.LocalDateTime createdOn, LocalDateTime publishedOn,
            Long userId, String userName,
            EventState state,
            Long confirmedRequests) {
        this.id = id;
        this.title = title;
        this.annotation = annotation;
        this.description = description;
        this.category = new CategoryDto(categoryId, categoryName);
        this.eventDate = eventDate;
        this.location = location;
        this.paid = paid;
        this.participantLimit = participantLimit;
        this.requestModeration = requestModeration;
        this.createdOn = createdOn;
        this.publishedOn = publishedOn;
        this.initiator = new UserShortDto(userId, userName);
        this.state = state;
        this.confirmedRequests = confirmedRequests;
        this.views = null;
    }

}
