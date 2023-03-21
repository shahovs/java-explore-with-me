package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorResponse {

    private final String message; // Сообщение об ошибке
    private final HttpStatus status; // Код статуса HTTP-ответа
    private final String reason; // Общее описание причины ошибки
    private final LocalDateTime timestamp; // Дата и время когда произошла ошибка
    private final StackTraceElement[] errors; // Список стектрейсов или описания ошибок

    public ErrorResponse(Throwable e, HttpStatus status, String reason) {
        message = e.getMessage();
        this.status = status;
        this.reason = reason;
        timestamp = LocalDateTime.now();
        errors = e.getStackTrace();
    }

}
