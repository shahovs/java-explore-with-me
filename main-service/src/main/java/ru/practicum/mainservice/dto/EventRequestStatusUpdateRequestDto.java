package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.model.ParticipationRequestStatus;

import java.util.List;

@Getter
@Setter
@ToString
public class EventRequestStatusUpdateRequestDto {
    private List<Long> requestIds;
    private ParticipationRequestStatus status;
}
