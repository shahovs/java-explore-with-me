package ru.practicum.ewm.server.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "hits")
public class Hit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String app;

    @Column(nullable = false, length = 80)
    private String uri;

    @Column(nullable = false, length = 40)
    private String ip;

    @Column(name = "request_time_stamp", nullable = false)
    private LocalDateTime requestTimeStamp;
}
