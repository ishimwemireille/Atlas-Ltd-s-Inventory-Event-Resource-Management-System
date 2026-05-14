package rw.auca.atlas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.atlas.model.Equipment;
import rw.auca.atlas.model.EquipmentStatus;

/**
 * REPOSITORY PATTERN: data access abstraction for Equipment.
 * Spring Data JPA generates SQL implementations from method name conventions.
 */
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

  // filter equipment by lifecycle status — used in status-based list views
  List<Equipment> findByStatus(EquipmentStatus status);

  // find equipment at or below the low-stock threshold — powers the dashboard alert panel
  List<Equipment> findByAvailableQuantityLessThanEqual(int threshold);
}
