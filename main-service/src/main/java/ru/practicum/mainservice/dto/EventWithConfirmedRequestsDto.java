package ru.practicum.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.model.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class EventWithConfirmedRequestsDto {

    private Event event;
    private Long confirmedRequests;

}
