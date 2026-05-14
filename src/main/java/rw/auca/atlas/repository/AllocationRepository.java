package rw.auca.atlas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.atlas.model.AllocationStatus;
import rw.auca.atlas.model.EquipmentAllocation;

/**
 * REPOSITORY PATTERN: data access abstraction for EquipmentAllocation.
 * Spring Data JPA generates SQL implementations from method name conventions.
 */
public interface AllocationRepository extends JpaRepository<EquipmentAllocation, Long> {

  // fetch all allocations linked to a specific event — used by the allocation list view
  List<EquipmentAllocation> findByEventId(Long eventId);

  // fetch all allocations for a specific piece of equipment — useful for equipment history
  List<EquipmentAllocation> findByEquipmentId(Long equipmentId);

  // count allocations by status — used in report summary aggregations
  long countByAllocationStatus(AllocationStatus status);
}
