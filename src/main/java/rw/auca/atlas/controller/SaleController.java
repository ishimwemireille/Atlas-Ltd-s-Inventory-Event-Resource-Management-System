package rw.auca.atlas.controller;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
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
import rw.auca.atlas.service.AuditLogService;

/**
 * Controller for recording and viewing equipment sales.
 * Accessible to all authenticated users (Admin and Staff).
 */
@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:5173")
public class SaleController {

  // REPOSITORY PATTERN: delegate all DB access through repository interfaces
  private final SaleRepository saleRepository;
  private final EquipmentRepository equipmentRepository;
  private final AuditLogService auditLogService;

  // constructor injection — avoids field injection for testability and immutability
  public SaleController(SaleRepository saleRepository, EquipmentRepository equipmentRepository,
      AuditLogService auditLogService) {
    this.saleRepository = saleRepository;
    this.equipmentRepository = equipmentRepository;
    this.auditLogService = auditLogService;
  }

  /**
   * Returns all sales records ordered by sale date descending.
   *
   * @return 200 OK with list of sales
   */
  @GetMapping
  public ResponseEntity<List<EquipmentSale>> getAllSales() {
    return ResponseEntity.ok(saleRepository.findAllByOrderBySaleDateDesc());
  }

  /**
   * Records a new equipment sale. Validates available stock before reducing inventory.
   * Writes an audit log entry on success.
   *
   * @param sale the sale data from the request body
   * @return 201 Created with the saved sale, or 400 Bad Request if stock is insufficient
   */
  @PostMapping
  public ResponseEntity<?> recordSale(@Valid @RequestBody EquipmentSale sale) {
    // validate input before hitting the database — load the equipment being sold
    Equipment equipment = equipmentRepository.findById(sale.getEquipment().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

    int qty = sale.getQuantitySold();

    // reject request early if stock is insufficient
    if (equipment.getAvailableQuantity() < qty) {
      return ResponseEntity.badRequest()
          .body(Map.of("message", "Not enough available stock to sell " + qty +
              " unit(s). Available: " + equipment.getAvailableQuantity()));
    }

    // permanently reduce inventory — sale removes units from both available and total counts
    equipment.setAvailableQuantity(equipment.getAvailableQuantity() - qty);
    equipment.setTotalQuantity(equipment.getTotalQuantity() - qty);

    // mark as RESERVED when available drops to zero after the sale
    if (equipment.getAvailableQuantity() == 0) {
      equipment.setStatus(rw.auca.atlas.model.EquipmentStatus.RESERVED);
    }
    equipmentRepository.save(equipment);

    sale.setEquipment(equipment);
    EquipmentSale saved = saleRepository.save(sale);

    // write an audit log entry for full traceability of every sale
    auditLogService.log("SALE", "Sale", saved.getId(),
        "Sold " + qty + " x " + equipment.getName() +
        (sale.getBuyerName() != null ? " to " + sale.getBuyerName() : ""));

    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }
}
