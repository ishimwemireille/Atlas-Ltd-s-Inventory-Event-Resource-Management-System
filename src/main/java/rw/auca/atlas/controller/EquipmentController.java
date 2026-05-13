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

  private final EquipmentService equipmentService;

  public EquipmentController(EquipmentService equipmentService) {
    this.equipmentService = equipmentService;
  }

  @GetMapping
  public ResponseEntity<List<Equipment>> getAllEquipment() {
    return ResponseEntity.ok(equipmentService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
    return ResponseEntity.ok(equipmentService.findById(id));
  }

  @PostMapping
  public ResponseEntity<Equipment> createEquipment(@Valid @RequestBody Equipment equipment) {
    return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.save(equipment));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Equipment> updateEquipment(
      @PathVariable Long id, @Valid @RequestBody Equipment equipment) {
    return ResponseEntity.ok(equipmentService.update(id, equipment));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
    equipmentService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/low-stock")
  public ResponseEntity<List<Equipment>> getLowStockEquipment() {
    return ResponseEntity.ok(equipmentService.findLowStock());
  }
}
