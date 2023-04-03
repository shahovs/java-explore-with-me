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
public class Location {

    @NotNull(message = "lat can't be null", groups = {Create.class})
    private Float lat;

    @NotNull(message = "lon can't be null", groups = {Create.class})
    private Float lon;

}
