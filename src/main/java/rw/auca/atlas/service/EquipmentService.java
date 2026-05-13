package rw.auca.atlas.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.atlas.exception.ResourceNotFoundException;
import rw.auca.atlas.model.Equipment;
import rw.auca.atlas.repository.EquipmentRepository;

/** Service layer for Equipment CRUD operations and low-stock queries. */
@Service
@Transactional
public class EquipmentService {

  private static final int LOW_STOCK_THRESHOLD = 2;

  // REPOSITORY PATTERN: data access abstraction
  private final EquipmentRepository equipmentRepository;

  public EquipmentService(EquipmentRepository equipmentRepository) {
    this.equipmentRepository = equipmentRepository;
  }

  /**
   * Returns all equipment items.
   *
   * @return list of all equipment
   */
  public List<Equipment> findAll() {
    return equipmentRepository.findAll();
  }

  /**
   * Finds equipment by its ID.
   *
   * @param id the equipment ID
   * @return the matching equipment
   * @throws ResourceNotFoundException if no equipment with that ID exists
   */
  public Equipment findById(Long id) {
    return equipmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
  }

  /**
   * Persists a new equipment record.
   *
   * @param equipment the equipment to save
   * @return the saved equipment with generated ID
   */
  public Equipment save(Equipment equipment) {
    return equipmentRepository.save(equipment);
  }

  /**
   * Updates an existing equipment record.
   *
   * @param id the ID of the equipment to update
   * @param updated the new field values
   * @return the updated equipment
   * @throws ResourceNotFoundException if no equipment with that ID exists
   */
  public Equipment update(Long id, Equipment updated) {
    Equipment existing = findById(id);
    existing.setName(updated.getName());
    existing.setDescription(updated.getDescription());
    existing.setCategory(updated.getCategory());
    existing.setTotalQuantity(updated.getTotalQuantity());
    existing.setAvailableQuantity(updated.getAvailableQuantity());
    existing.setStatus(updated.getStatus());
    return equipmentRepository.save(existing);
  }

  /**
   * Deletes an equipment record by its ID.
   *
   * @param id the ID of the equipment to delete
   * @throws ResourceNotFoundException if no equipment with that ID exists
   */
  public void delete(Long id) {
    if (!equipmentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Equipment not found with id: " + id);
    }
    equipmentRepository.deleteById(id);
  }

  /**
   * Returns equipment items with availableQuantity at or below the low-stock threshold.
   *
   * @return list of low-stock equipment
   */
  public List<Equipment> findLowStock() {
    return equipmentRepository.findByAvailableQuantityLessThanEqual(LOW_STOCK_THRESHOLD);
  }
}
