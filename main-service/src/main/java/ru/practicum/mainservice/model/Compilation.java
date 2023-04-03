package ru.practicum.mainservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@NamedEntityGraph(
        name = "compilation-entity-graph",
        attributeNodes = {
                @NamedAttributeNode(value = "eventsOfCompilation", subgraph = "events-subgraph"),
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "events-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("category"),
                                @NamedAttributeNode("initiator")
                        }
                )
        }
)

@Getter
@Setter
@ToString
@Entity
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compilation_id")
    private Long id;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false)
    private Boolean pinned;

    @ManyToMany
    @JoinTable(
            name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> eventsOfCompilation;

}
