package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import ru.practicum.mainservice.dto.UserDto;
import ru.practicum.mainservice.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserDto userDto);

    UserDto toDto(User user);

    List<UserDto> toDtos(List<User> users);

}
