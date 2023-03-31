package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.dto.ParticipationRequestDto;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.ParticipationRequest;
import ru.practicum.mainservice.model.ParticipationRequestStatus;
import ru.practicum.mainservice.model.User;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("select new ru.practicum.mainservice.dto.ParticipationRequestDto( " +
            "pr.id, pr.requester.id, pr.event.id, pr.created, pr.status) " +
            "from ParticipationRequest as pr " +
            "where pr.requester = :requester")
    List<ParticipationRequestDto> findAllByRequester(User requester);

    Integer countAllByEventAndStatus(Event event, ParticipationRequestStatus status);

}
