package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.exception.DuplicateException;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryServiceAdminImpl {

    private final CategoryRepository categoryRepository;
    // в категориях используем ModelMapper (в учебных целях),
    // а для остальных сущностей используем org.mapstruct.Mapper
    private final ModelMapper modelMapper;

    @Transactional
    public CategoryDto saveCategory(CategoryDto categoryDto) {
        Category category = modelMapper.map(categoryDto, Category.class);
        try {
            categoryRepository.save(category);
            return modelMapper.map(category, CategoryDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("Имя категории должно быть уникальным");
        }
    }

    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new ObjectNotFoundException("Категория не найдена или недоступна"));
        try {
            if (categoryDto.getName() != null) {
                category.setName(categoryDto.getName());
            }
            categoryRepository.save(category);
            return modelMapper.map(category, CategoryDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("Имя категории уже занято");
        }
    }

    public void deleteCategory(Long catId) {
        try {
            categoryRepository.deleteById(catId);
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException("Категория не найдена или недоступна");
        }
    }

    // метод для маппера EventMapper
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Категория не найдена"));
    }

}
