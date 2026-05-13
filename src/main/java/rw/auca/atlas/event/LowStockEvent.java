package rw.auca.atlas.event;

import org.springframework.context.ApplicationEvent;
import rw.auca.atlas.model.Equipment;

/**
 * OBSERVER PATTERN: published when equipment availableQuantity drops to 2 or below.
 * LowStockListener observes this event and logs the alert.
 */
public class LowStockEvent extends ApplicationEvent {

  private final Equipment equipment;

  public LowStockEvent(Object source, Equipment equipment) {
    super(source);
    this.equipment = equipment;
  }

  public Equipment getEquipment() {
    return equipment;
  }
}
