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

    // Если возвращать List<ParticipationRequest> вместо List<ParticipationRequestDto>, то будет несколько запросов
    // к репозиторию вместо одного (дополнительные запросы для получаения полных сущностей Event и др.)
    // Остается вопрос, можно ли не делать ручной запрос? Ведь поля у dto и у сущности называются одинаково.
    // Разница только в том, что в полях сущности лежат другие сущности, а в полях dto лежат только Long id этих сущностей
    // Можно ли как-то автоматически настроить запрос в базу только id вместо сущностей?
    @Query("select new ru.practicum.mainservice.dto.ParticipationRequestDto( " +
            "pr.id, pr.requester.id, pr.event.id, pr.created, pr.status) " +
            "from ParticipationRequest as pr " +
            "where pr.requester = :requester")
    List<ParticipationRequestDto> findAllByRequester(User requester);

    List<ParticipationRequest> findAllByEvent(Event event);
    List<ParticipationRequest> findAllByEventAndStatus(Event event, ParticipationRequestStatus status);
    Integer countAllByEventAndStatus(Event event, ParticipationRequestStatus status);
    Integer countAllByEvent(Event event);

}
