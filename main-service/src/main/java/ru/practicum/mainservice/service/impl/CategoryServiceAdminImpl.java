package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.exception.ConflictException;
import ru.practicum.mainservice.exception.DuplicateException;
import ru.practicum.mainservice.exception.ObjectNotFoundException;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryServiceAdminImpl {

    private final CategoryRepository categoryRepository;
    // в категориях используем ModelMapper (в учебных целях), а для остальных сущностей используем org.mapstruct.Mapper
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

    //    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new ObjectNotFoundException("Категория не найдена или недоступна"));
        try {
            if (categoryDto.getName() != null) {
                category.setName(categoryDto.getName());
            }
            categoryRepository.save(category);
//            categoryRepository.flush();
            // flush - чтобы поймать DataIntegrityViolationException (иначе не поймает) - это если поставить транзакцию
            return modelMapper.map(category, CategoryDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("Имя категории уже занято");
        }
    }

    //    @Transactional
    // Убираем транзацкию, иначе не будет поймано исключение DataIntegrityViolationException
    // а откатывать здесь все равно ничего не нужно
    public void deleteCategory(Long catId) {
        try {
            categoryRepository.deleteById(catId);
//            categoryRepository.flush();
            // flush - чтобы поймать DataIntegrityViolationException (иначе не поймает) - это если поставить транзакцию
        } catch (EmptyResultDataAccessException e) {
            throw new ObjectNotFoundException("Категория не найдена или недоступна");
        } catch (DataIntegrityViolationException e) {
            // todo наверное, не нужно бросать собственное исключение ConflictException,
            // так как мы точно не можем знать, чем было вызвано пойманное исключение DataIntegrityViolationException
            // (лучше передать DataIntegrityViolationException напрямую в errorHandler - так мы не сможем добавить
            // свое сообщение к ошибке, но зато уберем возможность передать неверное исключение)
            throw new ConflictException("Удаление категории невозможно. Существуют события, связанные с категорией");
        }
    }

    // метод для маппера EventMapper
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Категория не найдена"));
    }

}
