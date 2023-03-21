package ru.practicum.mainservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "participation_requests")
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_request_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ParticipationRequestStatus status;

    public ParticipationRequest(User requester, Event event) {
        this.requester = requester;
        this.event = event;
        created = LocalDateTime.now();
        if (event.getRequestModeration()) {
            status = ParticipationRequestStatus.PENDING;
        } else {
            status = ParticipationRequestStatus.CONFIRMED;
        }
    }

}
