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
import rw.auca.atlas.service.AllocationService;

/** REST controller exposing equipment allocation lifecycle endpoints. */
@RestController
@RequestMapping("/api/allocations")
@CrossOrigin(origins = "http://localhost:5173")
public class AllocationController {

  private final AllocationService allocationService;

  public AllocationController(AllocationService allocationService) {
    this.allocationService = allocationService;
  }

  /**
   * Allocates equipment to an event — triggers STATE PATTERN (IN_STOCK → RESERVED)
   * and optionally OBSERVER PATTERN (LowStockEvent).
   * Body: { "eventId": 1, "equipmentId": 2, "quantityAllocated": 3 }
   */
  @PostMapping
  public ResponseEntity<EquipmentAllocation> allocateEquipment(
      @RequestBody Map<String, Long> body) {
    Long eventId = body.get("eventId");
    Long equipmentId = body.get("equipmentId");
    int qty = body.get("quantityAllocated").intValue();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(allocationService.allocate(eventId, equipmentId, qty));
  }

  /** Deploys an allocation — triggers STATE PATTERN (RESERVED → DEPLOYED). */
  @PostMapping("/{id}/deploy")
  public ResponseEntity<Void> deployEquipment(@PathVariable Long id) {
    allocationService.deploy(id);
    return ResponseEntity.ok().build();
  }

  /** Returns allocated equipment — triggers STATE PATTERN (DEPLOYED → IN_STOCK). */
  @PostMapping("/{id}/return")
  public ResponseEntity<Void> returnEquipment(@PathVariable Long id) {
    allocationService.returnEquipment(id);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/event/{eventId}")
  public ResponseEntity<List<EquipmentAllocation>> getAllocationsByEvent(
      @PathVariable Long eventId) {
    return ResponseEntity.ok(allocationService.findByEvent(eventId));
  }
}
