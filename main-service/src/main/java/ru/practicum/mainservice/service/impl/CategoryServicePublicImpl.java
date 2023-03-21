package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServicePublicImpl {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public List<CategoryDto> getAllCategories(Integer fromElement, Integer size) {
        Pageable pageable = createPageRequest(fromElement, size);
        List<Category> categories = categoryRepository.findAll(pageable).toList();
        return categories.stream()
                .map(category -> modelMapper.map(category, CategoryDto.class))
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new ObjectNotFoundException("Категория не найдена или недоступна"));
        return modelMapper.map(category, CategoryDto.class);
    }

    private Pageable createPageRequest(int fromElement, Integer size) {
        int fromPage = fromElement / size;
        return PageRequest.of(fromPage, size);
    }

}
