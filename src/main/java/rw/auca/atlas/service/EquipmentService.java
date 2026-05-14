package rw.auca.atlas.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.atlas.exception.ResourceNotFoundException;
import rw.auca.atlas.model.Equipment;
import rw.auca.atlas.repository.EquipmentRepository;

/** Service layer for Equipment CRUD operations and low-stock queries. */
@Service
// @Transactional on the class level wraps every public method in a DB transaction
@Transactional
public class EquipmentService {

  // named constant — avoids magic number scattered across multiple call sites
  private static final int LOW_STOCK_THRESHOLD = 2;

  // REPOSITORY PATTERN: delegate all DB access through JPA repository interface
  private final EquipmentRepository equipmentRepository;

  // constructor injection — avoids field injection for testability and immutability
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
    // validate input before hitting the database — throw 404 if not found
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
    // @Valid on the controller ensures constraints are checked before this is called
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
    // load existing record first — prevents creating a new row with a supplied ID
    Equipment existing = findById(id);
    existing.setName(updated.getName());
    existing.setDescription(updated.getDescription());
    existing.setCategory(updated.getCategory());
    existing.setTotalQuantity(updated.getTotalQuantity());
    existing.setAvailableQuantity(updated.getAvailableQuantity());
    // update selling price — may be null if not set
    existing.setSellingPricePerUnit(updated.getSellingPricePerUnit());
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
    // validate input before hitting the database — fail fast if record does not exist
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
    // LOW_STOCK_THRESHOLD named constant avoids a magic number here
    return equipmentRepository.findByAvailableQuantityLessThanEqual(LOW_STOCK_THRESHOLD);
  }
}
