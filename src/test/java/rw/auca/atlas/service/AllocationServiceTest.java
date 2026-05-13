package rw.auca.atlas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import rw.auca.atlas.event.LowStockEvent;
import rw.auca.atlas.exception.InsufficientStockException;
import rw.auca.atlas.exception.ResourceNotFoundException;
import rw.auca.atlas.model.AllocationStatus;
import rw.auca.atlas.model.Equipment;
import rw.auca.atlas.model.EquipmentAllocation;
import rw.auca.atlas.model.EquipmentStatus;
import rw.auca.atlas.model.Event;
import rw.auca.atlas.repository.AllocationRepository;
import rw.auca.atlas.repository.EquipmentRepository;
import rw.auca.atlas.repository.EventRepository;

/**
 * Unit tests for {@link AllocationService}.
 *
 * <p>Covers both the STATE PATTERN (equipment status transitions)
 * and the OBSERVER PATTERN (LowStockEvent published when stock is low).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AllocationService — State & Observer Patterns")
class AllocationServiceTest {

  @Mock private AllocationRepository allocationRepository;
  @Mock private EquipmentRepository equipmentRepository;
  @Mock private EventRepository eventRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private AllocationService allocationService;

  private Equipment equipment;
  private Event event;

  @BeforeEach
  void setUp() {
    equipment = new Equipment();
    equipment.setName("Crown Amplifier");
    equipment.setTotalQuantity(6);
    equipment.setAvailableQuantity(6);
    equipment.setStatus(EquipmentStatus.IN_STOCK);

    event = new Event();
    event.setName("Kigali Summit");

    when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
    when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
    when(allocationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
  }

  // ── STATE PATTERN tests ────────────────────────────────────────────────────

  @Test
  @DisplayName("STATE PATTERN: allocate() reserves equipment and sets status to RESERVED when all stock taken")
  void allocate_allStock_setsStatusReserved() {
    allocationService.allocate(1L, 1L, 6);

    assertEquals(0, equipment.getAvailableQuantity());
    assertEquals(EquipmentStatus.RESERVED, equipment.getStatus());
  }

  @Test
  @DisplayName("STATE PATTERN: allocate() leaves status IN_STOCK when some stock remains")
  void allocate_partialStock_remainsInStock() {
    allocationService.allocate(1L, 1L, 4);

    assertEquals(2, equipment.getAvailableQuantity());
    assertEquals(EquipmentStatus.IN_STOCK, equipment.getStatus());
  }

  @Test
  @DisplayName("STATE PATTERN: allocate() throws InsufficientStockException when quantity exceeds available")
  void allocate_exceedsStock_throwsException() {
    assertThrows(
        InsufficientStockException.class,
        () -> allocationService.allocate(1L, 1L, 10)
    );
    verify(allocationRepository, never()).save(any());
  }

  @Test
  @DisplayName("STATE PATTERN: deploy() transitions equipment to DEPLOYED and allocation to DEPLOYED")
  void deploy_transitionsToDeployed() {
    EquipmentAllocation allocation = buildAllocation(AllocationStatus.RESERVED);
    when(allocationRepository.findById(1L)).thenReturn(Optional.of(allocation));

    allocationService.deploy(1L);

    assertEquals(EquipmentStatus.DEPLOYED, equipment.getStatus());
    assertEquals(AllocationStatus.DEPLOYED, allocation.getAllocationStatus());
  }

  @Test
  @DisplayName("STATE PATTERN: returnEquipment() restores stock and sets status IN_STOCK")
  void returnEquipment_restoresStockAndStatus() {
    equipment.setAvailableQuantity(0);
    equipment.setStatus(EquipmentStatus.DEPLOYED);
    EquipmentAllocation allocation = buildAllocation(AllocationStatus.DEPLOYED);
    when(allocationRepository.findById(1L)).thenReturn(Optional.of(allocation));

    allocationService.returnEquipment(1L);

    assertEquals(3, equipment.getAvailableQuantity());
    assertEquals(EquipmentStatus.IN_STOCK, equipment.getStatus());
    assertEquals(AllocationStatus.RETURNED, allocation.getAllocationStatus());
  }

  // ── OBSERVER PATTERN tests ─────────────────────────────────────────────────

  @Test
  @DisplayName("OBSERVER PATTERN: LowStockEvent is published when available quantity drops to 2")
  void allocate_stockAt2_publishesLowStockEvent() {
    equipment.setAvailableQuantity(4);

    allocationService.allocate(1L, 1L, 2);

    ArgumentCaptor<LowStockEvent> captor = ArgumentCaptor.forClass(LowStockEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());
    assertEquals(equipment, captor.getValue().getEquipment());
  }

  @Test
  @DisplayName("OBSERVER PATTERN: LowStockEvent is published when available quantity drops below 2")
  void allocate_stockBelow2_publishesLowStockEvent() {
    equipment.setAvailableQuantity(3);

    allocationService.allocate(1L, 1L, 2);

    verify(eventPublisher).publishEvent(any(LowStockEvent.class));
  }

  @Test
  @DisplayName("OBSERVER PATTERN: LowStockEvent is NOT published when available quantity stays above 2")
  void allocate_stockAbove2_doesNotPublishEvent() {
    equipment.setAvailableQuantity(6);

    allocationService.allocate(1L, 1L, 1);

    verify(eventPublisher, never()).publishEvent(any());
  }

  // ── ResourceNotFoundException tests ───────────────────────────────────────

  @Test
  @DisplayName("allocate() throws ResourceNotFoundException when equipment does not exist")
  void allocate_equipmentNotFound_throwsException() {
    when(equipmentRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> allocationService.allocate(1L, 99L, 1));
  }

  @Test
  @DisplayName("allocate() throws ResourceNotFoundException when event does not exist")
  void allocate_eventNotFound_throwsException() {
    when(eventRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> allocationService.allocate(99L, 1L, 1));
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private EquipmentAllocation buildAllocation(AllocationStatus status) {
    EquipmentAllocation allocation = new EquipmentAllocation();
    allocation.setEquipment(equipment);
    allocation.setEvent(event);
    allocation.setQuantityAllocated(3);
    allocation.setAllocationStatus(status);
    return allocation;
  }
}
