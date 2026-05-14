package rw.auca.atlas.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.atlas.event.LowStockEvent;
import rw.auca.atlas.exception.InsufficientStockException;
import rw.auca.atlas.exception.ResourceNotFoundException;
import rw.auca.atlas.model.AllocationStatus;
import rw.auca.atlas.model.Equipment;
import rw.auca.atlas.model.EquipmentAllocation;
import rw.auca.atlas.model.Event;
import java.math.BigDecimal;
import rw.auca.atlas.model.ReturnCondition;
import rw.auca.atlas.repository.AllocationRepository;
import rw.auca.atlas.repository.EquipmentRepository;
import rw.auca.atlas.repository.EventRepository;

/**
 * Service layer for equipment allocation lifecycle.
 * Demonstrates STATE PATTERN (via equipment state transitions) and
 * OBSERVER PATTERN (via LowStockEvent publishing).
 */
@Service
// @Transactional on the class level wraps every public method in a DB transaction
@Transactional
public class AllocationService {

  // named constant — avoids magic number scattered across call sites
  private static final int LOW_STOCK_THRESHOLD = 2;

  // REPOSITORY PATTERN: all data access goes through repository interfaces
  private final AllocationRepository allocationRepository;
  private final EquipmentRepository equipmentRepository;
  private final EventRepository eventRepository;
  // OBSERVER PATTERN: publisher fires domain events to registered listeners
  private final ApplicationEventPublisher eventPublisher;
  private final AuditLogService auditLogService;

  // constructor injection — avoids field injection for testability and immutability
  public AllocationService(
      AllocationRepository allocationRepository,
      EquipmentRepository equipmentRepository,
      EventRepository eventRepository,
      ApplicationEventPublisher eventPublisher,
      AuditLogService auditLogService) {
    this.allocationRepository = allocationRepository;
    this.equipmentRepository = equipmentRepository;
    this.eventRepository = eventRepository;
    this.eventPublisher = eventPublisher;
    this.auditLogService = auditLogService;
  }

  /**
   * Reserves equipment for an event. Applies STATE and OBSERVER patterns.
   *
   * @param eventId the ID of the target event
   * @param equipmentId the ID of the equipment to reserve
   * @param qty the number of units to reserve
   * @param rentalPricePerUnit optional rental price for this specific allocation
   * @return the saved EquipmentAllocation record
   * @throws ResourceNotFoundException if event or equipment does not exist
   * @throws InsufficientStockException if available stock is too low
   */
  public EquipmentAllocation allocate(Long eventId, Long equipmentId, int qty, BigDecimal rentalPricePerUnit) {
    // REPOSITORY PATTERN: fetch through repository interfaces — never access DB directly
    Equipment equipment = equipmentRepository.findById(equipmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + equipmentId));

    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

    try {
      // STATE PATTERN: reserve() transitions equipment from IN_STOCK → RESERVED
      equipment.reserve(qty);
    } catch (IllegalStateException exception) {
      // wrap IllegalStateException as domain-specific exception for cleaner error handling
      throw new InsufficientStockException(exception.getMessage());
    }

    equipmentRepository.save(equipment);

    // OBSERVER PATTERN: publish event when stock drops to critical level
    if (equipment.getAvailableQuantity() <= LOW_STOCK_THRESHOLD) {
      eventPublisher.publishEvent(new LowStockEvent(this, equipment));
    }

    EquipmentAllocation allocation = new EquipmentAllocation();
    allocation.setEquipment(equipment);
    allocation.setEvent(event);
    allocation.setQuantityAllocated(qty);
    allocation.setAllocationStatus(AllocationStatus.RESERVED);
    allocation.setRentalPricePerUnit(rentalPricePerUnit);

    return allocationRepository.save(allocation);
  }

  /**
   * Marks an allocation as deployed. Applies STATE PATTERN.
   *
   * @param allocationId the ID of the allocation to deploy
   * @throws ResourceNotFoundException if the allocation does not exist
   */
  public void deploy(Long allocationId) {
    EquipmentAllocation allocation = findAllocationById(allocationId);
    Equipment equipment = allocation.getEquipment();

    // STATE PATTERN: deploy() transitions equipment from RESERVED → DEPLOYED
    equipment.deploy();
    equipmentRepository.save(equipment);

    allocation.setAllocationStatus(AllocationStatus.DEPLOYED);
    // record exact timestamp when equipment left the warehouse
    allocation.setDeployedAt(LocalDateTime.now());
    allocationRepository.save(allocation);

    // write audit entry for full traceability of every deployment action
    auditLogService.log("DEPLOY", "Allocation", allocationId,
        "Deployed " + allocation.getQuantityAllocated() + " x " +
        allocation.getEquipment().getName() + " for event: " + allocation.getEvent().getName());
  }

  /**
   * Returns allocated equipment back to stock. Applies STATE PATTERN.
   *
   * @param allocationId the ID of the allocation being returned
   * @param condition the physical condition of the returned equipment
   * @param damageNotes optional notes describing any damage
   * @throws ResourceNotFoundException if the allocation does not exist
   */
  public void returnEquipment(Long allocationId, ReturnCondition condition, String damageNotes) {
    EquipmentAllocation allocation = findAllocationById(allocationId);
    Equipment equipment = allocation.getEquipment();

    // STATE PATTERN: returnStock() transitions equipment from DEPLOYED → IN_STOCK
    equipment.returnStock(allocation.getQuantityAllocated());
    equipmentRepository.save(equipment);

    allocation.setAllocationStatus(AllocationStatus.RETURNED);
    // record exact timestamp when equipment was returned
    allocation.setReturnedAt(LocalDateTime.now());
    allocation.setReturnCondition(condition);
    allocation.setDamageNotes(damageNotes);
    allocationRepository.save(allocation);

    String conditionStr = condition != null ? condition.name() : "GOOD";
    // write audit entry including return condition for accountability
    auditLogService.log("RETURN", "Allocation", allocationId,
        "Returned " + allocation.getQuantityAllocated() + " x " +
        equipment.getName() + " from event: " + allocation.getEvent().getName() +
        " — Condition: " + conditionStr);
  }

  /**
   * Returns all allocations for a given event.
   *
   * @param eventId the event ID to filter by
   * @return list of allocations for that event
   */
  public List<EquipmentAllocation> findByEvent(Long eventId) {
    // REPOSITORY PATTERN: delegate all DB access through JPA repository interface
    return allocationRepository.findByEventId(eventId);
  }

  // ── private helpers ────────────────────────────────────────────────────────

  private EquipmentAllocation findAllocationById(Long allocationId) {
    return allocationRepository.findById(allocationId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Allocation not found with id: " + allocationId));
  }
}
