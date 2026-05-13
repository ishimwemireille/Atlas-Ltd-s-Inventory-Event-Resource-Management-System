package rw.auca.atlas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.atlas.model.AllocationStatus;
import rw.auca.atlas.model.EquipmentAllocation;

// REPOSITORY PATTERN: data access abstraction for EquipmentAllocation
public interface AllocationRepository extends JpaRepository<EquipmentAllocation, Long> {

  List<EquipmentAllocation> findByEventId(Long eventId);

  List<EquipmentAllocation> findByEquipmentId(Long equipmentId);

  long countByAllocationStatus(AllocationStatus status);
}
