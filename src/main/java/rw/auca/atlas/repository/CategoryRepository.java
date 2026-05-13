package rw.auca.atlas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.atlas.model.Category;

// REPOSITORY PATTERN: data access abstraction for Category
public interface CategoryRepository extends JpaRepository<Category, Long> {}
