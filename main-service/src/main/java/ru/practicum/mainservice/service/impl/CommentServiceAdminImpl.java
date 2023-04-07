package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.mapper.CommentMapper;
import ru.practicum.mainservice.model.Comment;
import ru.practicum.mainservice.repository.CommentRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceAdminImpl {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public CommentDto patchComment(Long commentId, CommentDto commentDto) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new ObjectNotFoundException("Комментарий не найден или недоступен"));
        // text != null (это проверяется в контроллере с помощью javax.validation.constraints.NotBlank;
        comment.setText(commentDto.getText());
        commentRepository.save(comment);
        CommentDto resultDto = commentMapper.toDto(comment);
        return resultDto;
    }

    public void deleteComment(Long commentId) {
        try {
            commentRepository.deleteById(commentId);
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException("Комментарий не найден или недоступен");
        }
    }

}
