package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.Create;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class UserShortDto {

    @NotNull(groups = {Create.class})
    private Long id;

    @NotNull(groups = {Create.class})
    private String name;

}
