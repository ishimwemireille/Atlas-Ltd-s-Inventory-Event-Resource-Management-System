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
import rw.auca.atlas.repository.AllocationRepository;
import rw.auca.atlas.repository.EquipmentRepository;
import rw.auca.atlas.repository.EventRepository;

/**
 * Service layer for equipment allocation lifecycle.
 * Demonstrates STATE PATTERN (via equipment state transitions) and
 * OBSERVER PATTERN (via LowStockEvent publishing).
 */
@Service
@Transactional
public class AllocationService {

  // REPOSITORY PATTERN: all data access goes through repository interfaces
  private final AllocationRepository allocationRepository;
  private final EquipmentRepository equipmentRepository;
  private final EventRepository eventRepository;
  private final ApplicationEventPublisher eventPublisher;

  public AllocationService(
      AllocationRepository allocationRepository,
      EquipmentRepository equipmentRepository,
      EventRepository eventRepository,
      ApplicationEventPublisher eventPublisher) {
    this.allocationRepository = allocationRepository;
    this.equipmentRepository = equipmentRepository;
    this.eventRepository = eventRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Reserves equipment for an event. Applies STATE and OBSERVER patterns.
   *
   * @param eventId the ID of the target event
   * @param equipmentId the ID of the equipment to reserve
   * @param qty the number of units to reserve
   * @return the saved EquipmentAllocation record
   * @throws ResourceNotFoundException if event or equipment does not exist
   * @throws InsufficientStockException if available stock is too low
   */
  public EquipmentAllocation allocate(Long eventId, Long equipmentId, int qty) {
    // REPOSITORY PATTERN: fetch through repository interfaces
    Equipment equipment = equipmentRepository.findById(equipmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + equipmentId));

    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

    try {
      // STATE PATTERN: transition from IN_STOCK to RESERVED
      equipment.reserve(qty);
    } catch (IllegalStateException exception) {
      throw new InsufficientStockException(exception.getMessage());
    }

    equipmentRepository.save(equipment);

    // OBSERVER PATTERN: publish low stock event when available quantity drops to 2 or below
    if (equipment.getAvailableQuantity() <= 2) {
      eventPublisher.publishEvent(new LowStockEvent(this, equipment));
    }

    EquipmentAllocation allocation = new EquipmentAllocation();
    allocation.setEquipment(equipment);
    allocation.setEvent(event);
    allocation.setQuantityAllocated(qty);
    allocation.setAllocationStatus(AllocationStatus.RESERVED);

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

    // STATE PATTERN: transition from RESERVED to DEPLOYED
    equipment.deploy();
    equipmentRepository.save(equipment);

    allocation.setAllocationStatus(AllocationStatus.DEPLOYED);
    allocation.setDeployedAt(LocalDateTime.now());
    allocationRepository.save(allocation);
  }

  /**
   * Returns allocated equipment back to stock. Applies STATE PATTERN.
   *
   * @param allocationId the ID of the allocation being returned
   * @throws ResourceNotFoundException if the allocation does not exist
   */
  public void returnEquipment(Long allocationId) {
    EquipmentAllocation allocation = findAllocationById(allocationId);
    Equipment equipment = allocation.getEquipment();

    // STATE PATTERN: transition from DEPLOYED back to IN_STOCK
    equipment.returnStock(allocation.getQuantityAllocated());
    equipmentRepository.save(equipment);

    allocation.setAllocationStatus(AllocationStatus.RETURNED);
    allocation.setReturnedAt(LocalDateTime.now());
    allocationRepository.save(allocation);
  }

  /**
   * Returns all allocations for a given event.
   *
   * @param eventId the event ID to filter by
   * @return list of allocations for that event
   */
  public List<EquipmentAllocation> findByEvent(Long eventId) {
    return allocationRepository.findByEventId(eventId);
  }

  private EquipmentAllocation findAllocationById(Long allocationId) {
    return allocationRepository.findById(allocationId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Allocation not found with id: " + allocationId));
  }
}
