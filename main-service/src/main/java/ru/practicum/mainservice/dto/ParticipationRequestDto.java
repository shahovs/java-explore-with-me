package ru.practicum.mainservice.dto;

import lombok.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.Update;
import ru.practicum.mainservice.model.ParticipationRequestStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    private Long id;

    @NotNull(message = "requester can't be null", groups = {Create.class, Update.class})
    private Long requester;

    @NotNull(message = "event can't be null", groups = {Create.class, Update.class})
    private Long event;

    private LocalDateTime created;

    private ParticipationRequestStatus status;

}
