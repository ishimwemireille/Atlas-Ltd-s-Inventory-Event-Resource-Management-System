package rw.auca.atlas.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the STATE PATTERN implemented in {@link Equipment}.
 *
 * <p>Verifies every state transition:
 * IN_STOCK → RESERVED → DEPLOYED → IN_STOCK
 *
 * No Spring context needed — pure domain logic tests.
 */
@DisplayName("Equipment State Pattern")
class EquipmentStatePatternTest {

  private Equipment equipment;

  @BeforeEach
  void setUp() {
    equipment = new Equipment();
    equipment.setName("JBL Speaker");
    equipment.setTotalQuantity(10);
    equipment.setAvailableQuantity(10);
    equipment.setStatus(EquipmentStatus.IN_STOCK);
  }

  // ── reserve() ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("reserve() reduces available quantity and stays IN_STOCK when stock remains")
  void reserve_partialStock_remainsInStock() {
    equipment.reserve(4);

    assertEquals(6, equipment.getAvailableQuantity());
    assertEquals(EquipmentStatus.IN_STOCK, equipment.getStatus());
  }

  @Test
  @DisplayName("reserve() transitions to RESERVED when all stock is taken")
  void reserve_allStock_transitionsToReserved() {
    equipment.reserve(10);

    assertEquals(0, equipment.getAvailableQuantity());
    assertEquals(EquipmentStatus.RESERVED, equipment.getStatus());
  }

  @Test
  @DisplayName("reserve() throws IllegalStateException when requested quantity exceeds available")
  void reserve_exceedsAvailable_throwsException() {
    IllegalStateException ex = assertThrows(
        IllegalStateException.class,
        () -> equipment.reserve(15)
    );

    assertEquals(
        "Insufficient stock: requested 15, available 10",
        ex.getMessage()
    );
  }

  @Test
  @DisplayName("reserve() throws when equipment has zero available quantity")
  void reserve_zeroAvailable_throwsException() {
    equipment.setAvailableQuantity(0);

    assertThrows(IllegalStateException.class, () -> equipment.reserve(1));
  }

  // ── deploy() ──────────────────────────────────────────────────────────────

  @Test
  @DisplayName("deploy() transitions status to DEPLOYED")
  void deploy_transitionsToDeployed() {
    equipment.reserve(5);
    equipment.deploy();

    assertEquals(EquipmentStatus.DEPLOYED, equipment.getStatus());
  }

  // ── returnStock() ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("returnStock() restores available quantity and transitions back to IN_STOCK")
  void returnStock_restoresQuantityAndStatus() {
    equipment.reserve(10);
    equipment.deploy();
    equipment.returnStock(10);

    assertEquals(10, equipment.getAvailableQuantity());
    assertEquals(EquipmentStatus.IN_STOCK, equipment.getStatus());
  }

  @Test
  @DisplayName("returnStock() adds quantity back correctly when partially returned")
  void returnStock_partialReturn_addsQuantity() {
    equipment.reserve(6);
    equipment.deploy();
    equipment.returnStock(3);

    assertEquals(7, equipment.getAvailableQuantity());
    assertEquals(EquipmentStatus.IN_STOCK, equipment.getStatus());
  }

  // ── Full lifecycle ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("Full state lifecycle: IN_STOCK → RESERVED → DEPLOYED → IN_STOCK")
  void fullLifecycle_completesSuccessfully() {
    assertEquals(EquipmentStatus.IN_STOCK, equipment.getStatus());

    equipment.reserve(10);
    assertEquals(EquipmentStatus.RESERVED, equipment.getStatus());

    equipment.deploy();
    assertEquals(EquipmentStatus.DEPLOYED, equipment.getStatus());

    equipment.returnStock(10);
    assertEquals(EquipmentStatus.IN_STOCK, equipment.getStatus());
    assertEquals(10, equipment.getAvailableQuantity());
  }
}
