package rw.auca.atlas.event;

import org.springframework.context.ApplicationEvent;
import rw.auca.atlas.model.Equipment;

/**
 * OBSERVER PATTERN: domain event published when equipment availableQuantity drops
 * to the low-stock threshold (≤ 2).
 * LowStockListener observes this event and logs the warning alert.
 */
public class LowStockEvent extends ApplicationEvent {

  // carry the affected equipment so the listener can log its name and quantity
  private final Equipment equipment;

  public LowStockEvent(Object source, Equipment equipment) {
    super(source);
    this.equipment = equipment;
  }

  public Equipment getEquipment() {
    return equipment;
  }
}
