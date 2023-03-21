package ru.practicum.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.model.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class EventWithConfirmedRequestsDto {

    private Event event;
    private Long confirmedRequests;

    // если мы хотим, чтобы гибернейт все сделал одним запросом вместо двух (метод findEventsOneQuery)
    public EventWithConfirmedRequestsDto(
            Long id, String title, String annotation, String description,
            Long categoryId, String categoryName,
            LocalDateTime eventDate, Location location, Boolean paid, Integer participantLimit,
            Boolean requestModeration, java.time.LocalDateTime createdOn, LocalDateTime publishedOn,
            Long userId, String userName, String email,
            EventState state,
            Long confirmedRequests) {
        this.event = new Event(id, title, annotation, description,
                new Category(categoryId, categoryName),
                eventDate, location, paid, participantLimit,
                requestModeration, createdOn, publishedOn,
                new User(userId, userName, email),
                state, null);
        this.confirmedRequests = confirmedRequests;
    }

}
