package ru.practicum.mainservice.mapper;

import org.mapstruct.Mapper;
import ru.practicum.mainservice.dto.CategoryDto;
import ru.practicum.mainservice.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryDto categoryDto);

    CategoryDto toDto(Category category);

}
