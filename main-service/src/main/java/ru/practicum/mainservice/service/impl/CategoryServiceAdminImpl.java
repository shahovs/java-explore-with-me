package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceAdminImpl {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public CategoryDto saveCategory(CategoryDto categoryDto) {
        Category category = modelMapper.map(categoryDto, Category.class);
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDto.class);
    }

    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new ObjectNotFoundException("Категория не найдена")
        );
    }

}
