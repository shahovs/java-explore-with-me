package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.ParticipationRequest;
import ru.practicum.mainservice.model.ParticipationRequestStatus;
import ru.practicum.mainservice.model.User;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequester(User requester);
    List<ParticipationRequest> findAllByEvent(Event event);
    List<ParticipationRequest> findAllByEventAndStatus(Event event, ParticipationRequestStatus status);
    Integer countAllByEventAndStatus(Event event, ParticipationRequestStatus status);
    Integer countAllByEvent(Event event);

}
