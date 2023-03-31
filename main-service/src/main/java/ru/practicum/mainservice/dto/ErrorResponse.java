package ru.practicum.mainservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorResponse {

    private final String message; // Сообщение об ошибке
    private final HttpStatus status; // Код статуса HTTP-ответа
    private final String reason; // Общее описание причины ошибки
    private final LocalDateTime timestamp; // Дата и время когда произошла ошибка
    private final String errors; // Список стектрейсов или описания ошибок

    public ErrorResponse(Throwable e, HttpStatus status, String reason) {
        message = e.getMessage();
        this.status = status;
        this.reason = reason;
        timestamp = LocalDateTime.now();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        errors = stringWriter.toString();
    }

}
