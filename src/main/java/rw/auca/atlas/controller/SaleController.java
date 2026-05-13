package rw.auca.atlas.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.atlas.exception.ResourceNotFoundException;
import rw.auca.atlas.model.Equipment;
import rw.auca.atlas.model.EquipmentSale;
import rw.auca.atlas.repository.EquipmentRepository;
import rw.auca.atlas.repository.SaleRepository;

/** Controller for recording and viewing equipment sales. Accessible to all authenticated users. */
@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:5173")
public class SaleController {

  private final SaleRepository saleRepository;
  private final EquipmentRepository equipmentRepository;

  public SaleController(SaleRepository saleRepository, EquipmentRepository equipmentRepository) {
    this.saleRepository = saleRepository;
    this.equipmentRepository = equipmentRepository;
  }

  @GetMapping
  public ResponseEntity<List<EquipmentSale>> getAllSales() {
    return ResponseEntity.ok(saleRepository.findAllByOrderBySaleDateDesc());
  }

  @PostMapping
  public ResponseEntity<?> recordSale(@RequestBody EquipmentSale sale) {
    Equipment equipment = equipmentRepository.findById(sale.getEquipment().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

    int qty = sale.getQuantitySold();

    if (equipment.getAvailableQuantity() < qty) {
      return ResponseEntity.badRequest()
          .body(Map.of("message", "Not enough available stock to sell " + qty + " unit(s). Available: " + equipment.getAvailableQuantity()));
    }

    // Permanently reduce both total and available quantity
    equipment.setAvailableQuantity(equipment.getAvailableQuantity() - qty);
    equipment.setTotalQuantity(equipment.getTotalQuantity() - qty);

    // Update status based on remaining available quantity
    if (equipment.getAvailableQuantity() == 0) {
      equipment.setStatus(rw.auca.atlas.model.EquipmentStatus.RESERVED);
    }

    equipmentRepository.save(equipment);

    sale.setEquipment(equipment);
    EquipmentSale saved = saleRepository.save(sale);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }
}
