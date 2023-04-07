package ru.practicum.mainservice.dto;

import lombok.*;
import ru.practicum.mainservice.Create;
import ru.practicum.mainservice.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;

    private UserShortDto commentator;

    private Long event;

    @NotBlank(message = "text can't be null, empty or blank", groups = {Create.class, Update.class})
    @Size(max = 400, message = "text must have 400 characters maximum", groups = {Create.class, Update.class})
    private String text;

    private LocalDateTime created;

    @Getter
    @Setter
    public static class UserShortDto {
        private Long id;
        private String name;
    }

}
