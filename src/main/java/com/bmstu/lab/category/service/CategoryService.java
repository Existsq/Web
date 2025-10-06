package com.bmstu.lab.category.service;

import com.bmstu.lab.calculate.cpi.service.CalculateCpiService;
import com.bmstu.lab.category.exception.CategoryAlreadyExistException;
import com.bmstu.lab.category.exception.CategoryNotFoundException;
import com.bmstu.lab.category.model.dto.CategoryDTO;
import com.bmstu.lab.category.model.entity.Category;
import com.bmstu.lab.category.model.mapper.CategoryMapper;
import com.bmstu.lab.category.repository.CategoryRepository;
import com.bmstu.lab.minio.MinioTemplate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Сервис для работы с сущностью {@link Category}.
 *
 * <p>Предоставляет методы для поиска, создания, обновления и удаления категорий, а также добавления
 * категорий в "черновик" (draft) пользователя. Управляет взаимодействием с хранилищем файлов через
 * {@link MinioTemplate} и бизнес-логикой корзины через {@link CalculateCpiService}.
 */
@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final MinioTemplate minioTemplate;

  /**
   * Конструктор сервиса.
   *
   * @param categoryRepository репозиторий для работы с категориями
   * @param minioTemplate шаблон для работы с файлами в MinIO
   */
  public CategoryService(CategoryRepository categoryRepository, MinioTemplate minioTemplate) {
    this.categoryRepository = categoryRepository;
    this.minioTemplate = minioTemplate;
  }

  /**
   * Возвращает список всех категорий или категории, фильтрованные по названию.
   *
   * <p>Если title == null, возвращает все категории. Если фильтр задан, возвращает категории,
   * содержащие title (игнорируя регистр). Может вернуть пустой список.
   *
   * @param title фильтр по названию категории, если null – возвращает все категории
   * @return список DTO категорий, возможно пустой
   */
  public List<CategoryDTO> findAll(String title) {
    if (title == null) {
      return categoryRepository.findAll().stream().map(CategoryMapper::toDto).toList();
    } else {
      return categoryRepository.findByTitleContainingIgnoreCase(title).stream()
          .map(CategoryMapper::toDto)
          .toList();
    }
  }

  /**
   * Находит категорию по ID и возвращает сущность.
   *
   * @param id ID категории
   * @return сущность Category
   * @throws CategoryNotFoundException если категория не найдена
   */
  public Category findByIdEntity(Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
  }

  /**
   * Находит категорию по ID и возвращает DTO.
   *
   * @param categoryId ID категории
   * @return DTO категории
   * @throws CategoryNotFoundException если категория не найдена
   */
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

  /**
   * Создает новую категорию.
   *
   * @param categoryDTO DTO категории
   * @return DTO созданной категории
   * @throws CategoryAlreadyExistException если категория с таким ID уже существует
   */
  public CategoryDTO create(CategoryDTO categoryDTO) {
    if (categoryDTO.getId() != null && categoryRepository.existsById(categoryDTO.getId())) {
      throw new CategoryAlreadyExistException(
          "Категория с id = " + categoryDTO.getId() + " уже существует");
    }

    Category category = CategoryMapper.toEntity(categoryDTO);
    return CategoryMapper.toDto(categoryRepository.save(category));
  }

  /**
   * Обновляет существующую категорию.
   *
   * @param categoryId ID категории
   * @param categoryDTO DTO с обновленными данными
   * @return DTO обновленной категории
   * @throws CategoryNotFoundException если категория не найдена
   */
  public CategoryDTO update(Long categoryId, CategoryDTO categoryDTO) {
    Category existingCategory =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));

    copyToEntity(existingCategory, categoryDTO);

    return CategoryMapper.toDto(categoryRepository.save(existingCategory));
  }

  /**
   * Удаляет категорию.
   *
   * @param categoryId ID категории
   * @throws CategoryNotFoundException если категория не найдена
   */
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

  /**
   * Загружает изображение для категории и сохраняет его в MinIO.
   *
   * @param categoryId ID категории
   * @param file файл изображения
   * @return DTO категории с обновленным изображением
   * @throws CategoryNotFoundException если категория не найдена
   */
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

  /**
   * Копирует значения из DTO в сущность.
   *
   * @param entity сущность категории
   * @param dto DTO категории
   */
  private void copyToEntity(Category entity, CategoryDTO dto) {
    entity.setTitle(dto.getTitle());
    entity.setBasePrice(dto.getBasePrice());
    entity.setImageId(dto.getImageId());
    entity.setDescription(dto.getDescription());
    entity.setShortDescription(dto.getShortDescription());
    entity.setStatus(dto.getStatus());
  }
}
