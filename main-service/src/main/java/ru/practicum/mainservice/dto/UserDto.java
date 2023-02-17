package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.mainservice.Create;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class UserDto {

    private Long id;

    @NotBlank(groups = {Create.class})
    private String name;

    @NotNull(groups = {Create.class})
    @Email(groups = {Create.class})
    private String email;

}
