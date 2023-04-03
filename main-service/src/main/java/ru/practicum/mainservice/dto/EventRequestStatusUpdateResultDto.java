package ru.practicum.mainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.Update;
import ru.practicum.mainservice.model.ParticipationRequestStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class EventRequestStatusUpdateResultDto {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;

    @Getter
    @Setter
    public static class ParticipationRequestDto {

        private Long id;

        @NotNull(message = "requester can't be null", groups = {Create.class, Update.class})
        private Long requester;

        @NotNull(message = "event can't be null", groups = {Create.class, Update.class})
        private Long event;

        private LocalDateTime created;

        private ParticipationRequestStatus status;
    }

}
