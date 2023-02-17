package ru.practicum.mainservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.Create;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@Embeddable
//@AttributeOverrides({
//        @AttributeOverride( name = "lat", column = @Column(name = "lat")),
//        @AttributeOverride( name = "lon", column = @Column(name = "lon"))
//})
public class Location {

    @NotNull(message = "lat can't be null", groups = {Create.class})
    private Float lat;

    @NotNull(message = "lon can't be null", groups = {Create.class})
    private Float lon;

}

/*
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lon;

}*/
