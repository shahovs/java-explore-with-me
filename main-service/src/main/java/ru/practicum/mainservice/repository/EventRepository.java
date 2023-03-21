package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.dto.EventFullDto;
import ru.practicum.mainservice.dto.EventShortDto;
import ru.practicum.mainservice.dto.EventWithConfirmedRequestsDto;
import ru.practicum.mainservice.model.Event;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    @Query("select new ru.practicum.mainservice.dto.EventShortDto (" +
            "e.id, e.title, e.annotation, e.category.id, e.category.name, e.eventDate, e.paid, " +
            "e.initiator.id, e.initiator.name, count (pr)) " +
            "from Event e " +
            "left join ParticipationRequest pr on pr.event = e " +
            "where e.id in :eventIds and (pr.status = 'CONFIRMED' or pr = null) " +
            "group by e, e.category.name, e.initiator.name") // or "group by e"
    List<EventShortDto> findEventDtosByIdWithConfirmedRequests(List<Long> eventIds);

    @Query("select new ru.practicum.mainservice.dto.EventWithConfirmedRequestsDto (e, count (pr)) " +
            "from Event e " +
            "left join fetch ParticipationRequest pr on pr.event = e " +
            "where e.id in :eventIds and (pr.status = 'CONFIRMED' or pr = null) " +
            "group by e")
    List<EventWithConfirmedRequestsDto> findEvents(List<Long> eventIds);

    // если мы хотим, чтобы гибернейт все сделал одним запросом вместо двух
    @Query("select new ru.practicum.mainservice.dto.EventWithConfirmedRequestsDto( " +
            "e.id, e.title, e.annotation, e.description, " +
            "c.id, c.name, " +
            "e.eventDate, e.location, e.paid, e.participantLimit, e.requestModeration, e.createdOn, e.publishedOn, " +
            "u.id, u.name, u.email, " +
            "e.state, " +
            "count (pr) " +
            ") " +
            "from Event e " +
            "left join Category c on e.category = c " +
            "left join User u on e.initiator = u " +
            "left join ParticipationRequest pr on pr.event = e " +
            // todo странный тест 409 Conflict
            //  Добавление запроса на участие в событии, у которого заполнен лимит участников
            // Тест создает запрос на участие, но не подтверждает его, а сразу создает второй запрос на участие
            // То есть ограничивается подача заявок на участие общим лимитом (даже до их подтверждения)
            // То есть если какие-то заявки будут отклонены, то получится, что оставшихся заявок меньше, чем лимит участия
//            "where e.id in :eventIds and (pr.status = 'CONFIRMED' or pr = null) " +
            "where e.id in :eventIds and (pr.status != 'CONFIRMED' or pr = null) " +
//            "where e.id in :eventIds " +
            "group by e, c, u")
    List<EventWithConfirmedRequestsDto> findEventsOneQuery(List<Long> eventIds);
    //PENDING, CONFIRMED, REJECTED, CANCELED

    @Query("select new ru.practicum.mainservice.dto.EventFullDto( " +
            "e.id, e.title, e.annotation, e.description, " +
            "c.id, c.name, " +
            "e.eventDate, e.location, e.paid, e.participantLimit, e.requestModeration, e.createdOn, e.publishedOn, " +
            "u.id, u.name, " +
            "e.state, " +
            "count (pr) " +
            ") " +
            "from Event e " +
            "left join Category c on e.category = c " +
            "left join User u on e.initiator = u " +
            "left join ParticipationRequest pr on pr.event = e " +
            "where e.id in :eventIds and (pr.status = 'CONFIRMED' or pr = null) " +
            "group by e, c, u")
    List<EventFullDto> findEventsFullDto(List<Long> eventIds);

}
