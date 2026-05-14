package rw.auca.atlas.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.atlas.model.EquipmentAllocation;
import rw.auca.atlas.model.ReturnCondition;
import rw.auca.atlas.service.AllocationService;

/** REST controller exposing equipment allocation lifecycle endpoints. */
@RestController
@RequestMapping("/api/allocations")
@CrossOrigin(origins = "http://localhost:5173")
public class AllocationController {

  // constructor injection — avoids field injection for testability and immutability
  private final AllocationService allocationService;

  public AllocationController(AllocationService allocationService) {
    this.allocationService = allocationService;
  }

  /**
   * Allocates equipment to an event — triggers STATE PATTERN (IN_STOCK → RESERVED)
   * and optionally OBSERVER PATTERN (LowStockEvent when stock drops to threshold).
   * Body: { "eventId": 1, "equipmentId": 2, "quantityAllocated": 3 }
   */
  @PostMapping
  public ResponseEntity<EquipmentAllocation> allocateEquipment(
      @RequestBody Map<String, Object> body) {
    // extract typed values from the generic map payload
    Long eventId = Long.valueOf(body.get("eventId").toString());
    Long equipmentId = Long.valueOf(body.get("equipmentId").toString());
    int qty = Integer.parseInt(body.get("quantityAllocated").toString());

    // rental price is optional — null if not supplied by the client
    java.math.BigDecimal rentalPrice = null;
    if (body.containsKey("rentalPricePerUnit") && body.get("rentalPricePerUnit") != null) {
      rentalPrice = new java.math.BigDecimal(body.get("rentalPricePerUnit").toString());
    }
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(allocationService.allocate(eventId, equipmentId, qty, rentalPrice));
  }

  /** Deploys an allocation — triggers STATE PATTERN (RESERVED → DEPLOYED). */
  @PostMapping("/{id}/deploy")
  public ResponseEntity<Void> deployEquipment(@PathVariable Long id) {
    allocationService.deploy(id);
    return ResponseEntity.ok().build();
  }

  /**
   * Returns allocated equipment — triggers STATE PATTERN (DEPLOYED → IN_STOCK).
   * Body (optional): { "condition": "GOOD|DAMAGED|MISSING_PARTS", "damageNotes": "..." }
   */
  @PostMapping("/{id}/return")
  public ResponseEntity<Void> returnEquipment(
      @PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
    // default to GOOD condition when no body is provided
    ReturnCondition condition = ReturnCondition.GOOD;
    String damageNotes = null;
    if (body != null) {
      if (body.containsKey("condition")) {
        // safely parse condition — ignore unknown values and fall back to GOOD
        try { condition = ReturnCondition.valueOf(body.get("condition")); } catch (Exception ignored) {}
      }
      damageNotes = body.get("damageNotes");
    }
    allocationService.returnEquipment(id, condition, damageNotes);
    return ResponseEntity.ok().build();
  }

  /** Returns all allocations for a given event. */
  @GetMapping("/event/{eventId}")
  public ResponseEntity<List<EquipmentAllocation>> getAllocationsByEvent(
      @PathVariable Long eventId) {
    // REPOSITORY PATTERN: delegate all DB access through the service/repository chain
    return ResponseEntity.ok(allocationService.findByEvent(eventId));
  }
}
