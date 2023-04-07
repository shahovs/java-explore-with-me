package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.mapper.CommentMapper;
import ru.practicum.mainservice.model.Comment;
import ru.practicum.mainservice.model.Event;
import ru.practicum.mainservice.repository.CommentRepository;
import ru.practicum.mainservice.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServicePublicImpl {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new ObjectNotFoundException("Комментарий не найден или не доступен"));
        CommentDto commentDto = commentMapper.toDto(comment);
        return commentDto;
    }

    public List<CommentDto> getCommentsByEvent(Long eventId, Pageable pageable) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Событие не найдено"));
        List<Comment> comments = commentRepository.findByEvent(event, pageable);
        List<CommentDto> commentDtos = commentMapper.toDtos(comments);
        return commentDtos;
    }

}
