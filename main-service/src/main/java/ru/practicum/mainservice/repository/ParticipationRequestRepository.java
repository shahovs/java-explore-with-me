package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.mainservice.model.ParticipationRequest;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

}
