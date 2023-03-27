package ru.practicum.mainservice.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@NamedEntityGraph(
        name = "event-entity-graph",
        attributeNodes = {
                @NamedAttributeNode("category"),
                @NamedAttributeNode("initiator"),
                @NamedAttributeNode("requests"),
        }
)

@Getter
@Setter
@ToString
@Entity
@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Embedded
    private Location location;

    private Boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private EventState state;

    @OneToMany(mappedBy = "event")
    private List<ParticipationRequest> requests;

//    @ManyToMany(mappedBy = "eventsOfCompilation")
//    private List<Compilation> myCompilations;

}
