package rw.auca.atlas.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.atlas.model.Equipment;
import rw.auca.atlas.service.EquipmentService;

/** REST controller exposing equipment management endpoints. */
@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "http://localhost:5173")
public class EquipmentController {

  // REPOSITORY PATTERN: delegate all business logic through the service layer
  private final EquipmentService equipmentService;

  // constructor injection — avoids field injection for testability and immutability
  public EquipmentController(EquipmentService equipmentService) {
    this.equipmentService = equipmentService;
  }

  /**
   * Returns all equipment items in the system.
   *
   * @return 200 OK with list of equipment
   */
  @GetMapping
  public ResponseEntity<List<Equipment>> getAllEquipment() {
    return ResponseEntity.ok(equipmentService.findAll());
  }

  /**
   * Returns a single equipment item by ID.
   *
   * @param id the equipment ID
   * @return 200 OK with the equipment, or 404 if not found
   */
  @GetMapping("/{id}")
  public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
    return ResponseEntity.ok(equipmentService.findById(id));
  }

  /**
   * Creates a new equipment record. Input is validated via {@code @Valid} before persistence.
   *
   * @param equipment the equipment data from the request body
   * @return 201 Created with the saved equipment
   */
  @PostMapping
  public ResponseEntity<Equipment> createEquipment(@Valid @RequestBody Equipment equipment) {
    // @Valid triggers bean-validation — rejects request if @NotBlank/@Min constraints fail
    return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.save(equipment));
  }

  /**
   * Updates an existing equipment record.
   *
   * @param id        the ID of the equipment to update
   * @param equipment the updated equipment data
   * @return 200 OK with the updated equipment, or 404 if not found
   */
  @PutMapping("/{id}")
  public ResponseEntity<Equipment> updateEquipment(
      @PathVariable Long id, @Valid @RequestBody Equipment equipment) {
    // @Valid ensures updated data still satisfies all validation constraints
    return ResponseEntity.ok(equipmentService.update(id, equipment));
  }

  /**
   * Deletes an equipment record by ID.
   *
   * @param id the ID of the equipment to delete
   * @return 204 No Content on success, or 404 if not found
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
    equipmentService.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Returns equipment items with available quantity at or below the low-stock threshold.
   * Used by the dashboard to surface stock alerts.
   *
   * @return 200 OK with list of low-stock equipment
   */
  @GetMapping("/low-stock")
  public ResponseEntity<List<Equipment>> getLowStockEquipment() {
    // OBSERVER PATTERN: low-stock data is used by the dashboard to surface alerts
    return ResponseEntity.ok(equipmentService.findLowStock());
  }
}
