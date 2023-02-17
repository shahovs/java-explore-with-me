package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.UserDto;
import ru.practicum.mainservice.exception.DuplicateException;
import ru.practicum.mainservice.mapper.UserMapper;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceAdminImpl {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserDto> getAllUsersByIds(List<Long> userIds, Integer fromElement, Integer size) {
        int fromPage = fromElement / size;
        Pageable pageable = PageRequest.of(fromPage, size);
        List<User> users = userRepository.findAllByIdIn(userIds, pageable);
        return userMapper.toDtos(users);
    }

    @Transactional
    public UserDto saveUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        try {
            User savedUser = userRepository.save(user);
            return userMapper.toDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("Пользователь с таким именем или email уже существует");
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

}