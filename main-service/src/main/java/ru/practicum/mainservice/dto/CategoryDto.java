package ru.practicum.mainservice.dto;

import lombok.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.Update;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;

    @NotNull(groups = {Create.class, Update.class})
    private String name;

}
