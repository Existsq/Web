package com.bmstu.lab.category.service;

import com.bmstu.lab.category.model.dto.CategoryDTO;
import com.bmstu.lab.category.model.entity.Category;
import com.bmstu.lab.category.model.mapper.CategoryMapper;
import com.bmstu.lab.category.exception.CategoryAlreadyExistException;
import com.bmstu.lab.category.exception.CategoryNotFoundException;
import com.bmstu.lab.minio.MinioTemplate;
import com.bmstu.lab.category.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final MinioTemplate minioTemplate;

  public CategoryService(CategoryRepository categoryRepository, MinioTemplate minioTemplate) {
    this.categoryRepository = categoryRepository;
    this.minioTemplate = minioTemplate;
  }

  public List<CategoryDTO> findAll(String title) {
    if (title == null) {
      return categoryRepository.findAll().stream().map(CategoryMapper::toDto).toList();
    } else {
      return categoryRepository.findByTitleContainingIgnoreCase(title).stream()
          .map(CategoryMapper::toDto)
          .toList();
    }
  }

  public CategoryDTO findById(Long categoryId) {
    Category existingCategory =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(
                () ->
                    new CategoryNotFoundException(
                        "Категория с id = " + categoryId + " не найдена"));
    return CategoryMapper.toDto(existingCategory);
  }

  public CategoryDTO create(CategoryDTO categoryDTO) {
    if (categoryDTO.getId() != null && categoryRepository.existsById(categoryDTO.getId())) {
      throw new CategoryAlreadyExistException(
          "Категория с id = " + categoryDTO.getId() + " уже существует");
    }

    Category category = CategoryMapper.toEntity(categoryDTO);
    return CategoryMapper.toDto(categoryRepository.save(category));
  }

  public CategoryDTO addCategoryToDraft(Long categoryId) {
    // TODO использовать метод в сервисе заявок
    return new CategoryDTO();
  }

  public CategoryDTO update(Long categoryId, CategoryDTO categoryDTO) {
    Category existingCategory =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));

    copyToEntity(existingCategory, categoryDTO);

    return CategoryMapper.toDto(categoryRepository.save(existingCategory));
  }

  public void delete(Long categoryId) {
    Category existingCategory =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));

    if (existingCategory.getImageId() != null) {
      minioTemplate.deleteFile(existingCategory.getImageId());
    }

    categoryRepository.delete(existingCategory);
  }

  public CategoryDTO addImage(Long categoryId, MultipartFile file) {
    Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));

    if (category.getImageId() != null) {
      minioTemplate.deleteFile(category.getImageId());
    }

    String fileName = minioTemplate.uploadFile(file);

    category.setImageId(fileName);
    return CategoryMapper.toDto(categoryRepository.save(category));
  }

  private void copyToEntity(Category entity, CategoryDTO dto) {
    entity.setTitle(dto.getTitle());
    entity.setBasePrice(dto.getBasePrice());
    entity.setImageId(dto.getImageId());
    entity.setDescription(dto.getDescription());
    entity.setShortDescription(dto.getShortDescription());
    entity.setCoefficient(dto.getCoefficient());
    entity.setStatus(dto.getStatus());
  }
}
