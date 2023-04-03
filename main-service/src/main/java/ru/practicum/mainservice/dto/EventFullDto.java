package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.Update;
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

    @Getter
    @Setter
    public static class CategoryDto {

        private Long id;

        @NotNull(groups = {Create.class, Update.class})
        private String name;
    }

    @Getter
    @Setter
    public static class UserShortDto {

        @NotNull(groups = {Create.class})
        private Long id;

        @NotNull(groups = {Create.class})
        private String name;
    }

}
