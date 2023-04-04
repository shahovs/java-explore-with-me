package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.exception.ValidateException;
import ru.practicum.mainservice.mapper.CommentMapper;
import ru.practicum.mainservice.model.Comment;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.model.EventState;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.repository.CommentRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.UserRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServicePrivateImpl {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    public CommentDto saveComment(Long commentatorId, Long eventId, CommentDto commentDto) {
        User commentator = userRepository.findById(commentatorId).orElseThrow(
                () -> new ObjectNotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        validateEvent(event);
        Comment comment = new Comment(commentator, event, commentDto.getText());
        commentRepository.save(comment);
        CommentDto resultDto = commentMapper.toDto(comment);
        return resultDto;
    }

    private void validateEvent(Event event) {
        if (!Objects.equals(event.getState(), EventState.PUBLISHED)) {
            throw new ValidateException("Ошибка. Нельзя оставлять комментарии к неопубликованным событиям");
        }
    }

    public CommentDto patchComment(Long commentatorId, Long commentId, CommentDto commentDto) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new ObjectNotFoundException("Комментарий не найден или недоступен"));
        validateCommentator(commentatorId, comment);
        // text != null (это проверяется в контроллере с помощью javax.validation.constraints.NotBlank;
        comment.setText(commentDto.getText());
        commentRepository.save(comment);
        CommentDto resultDto = commentMapper.toDto(comment);
        return resultDto;
    }

    private void validateCommentator(Long commentatorId, Comment comment) {
        if (!Objects.equals(commentatorId, comment.getCommentator().getId())) {
            throw new ValidateException("Комментарий может изменить или удалить только его автор");
        }
    }

    public void deleteComment(Long commentatorId, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new ObjectNotFoundException("Комментарий не найден или недоступен"));
        validateCommentator(commentatorId, comment);
        commentRepository.delete(comment);
    }

}
