package rw.auca.atlas.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.atlas.exception.ResourceNotFoundException;
import rw.auca.atlas.model.Category;
import rw.auca.atlas.repository.CategoryRepository;

/** Service layer for Category CRUD operations. */
@Service
@Transactional
public class CategoryService {

  // REPOSITORY PATTERN: data access abstraction
  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  /**
   * Returns all equipment categories.
   *
   * @return list of all categories
   */
  public List<Category> findAll() {
    return categoryRepository.findAll();
  }

  /**
   * Finds a category by its ID.
   *
   * @param id the category ID
   * @return the matching category
   * @throws ResourceNotFoundException if no category with that ID exists
   */
  public Category findById(Long id) {
    return categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
  }

  /**
   * Persists a new category.
   *
   * @param category the category to save
   * @return the saved category with generated ID
   */
  public Category save(Category category) {
    return categoryRepository.save(category);
  }

  /**
   * Deletes a category by its ID.
   *
   * @param id the ID of the category to delete
   * @throws ResourceNotFoundException if no category with that ID exists
   */
  public void delete(Long id) {
    if (!categoryRepository.existsById(id)) {
      throw new ResourceNotFoundException("Category not found with id: " + id);
    }
    categoryRepository.deleteById(id);
  }
}
