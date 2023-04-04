package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.mainservice.dto.CommentDto;
import ru.practicum.mainservice.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "event", source = "event.id")
    CommentDto toDto(Comment comment);

    List<CommentDto> toDtos(List<Comment> comments);

}
