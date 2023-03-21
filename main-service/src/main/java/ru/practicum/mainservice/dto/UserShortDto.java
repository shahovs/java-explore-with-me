package ru.practicum.mainservice.dto;

import lombok.*;
import ru.practicum.mainservice.Create;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {

    @NotNull(groups = {Create.class})
    private Long id;

    @NotNull(groups = {Create.class})
    private String name;

}
