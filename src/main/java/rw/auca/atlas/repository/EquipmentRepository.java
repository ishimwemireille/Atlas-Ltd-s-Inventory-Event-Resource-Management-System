package rw.auca.atlas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.atlas.model.Equipment;
import rw.auca.atlas.model.EquipmentStatus;

// REPOSITORY PATTERN: data access abstraction for Equipment
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

  List<Equipment> findByStatus(EquipmentStatus status);

  List<Equipment> findByAvailableQuantityLessThanEqual(int threshold);
}
