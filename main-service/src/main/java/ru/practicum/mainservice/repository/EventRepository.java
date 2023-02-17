package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}
