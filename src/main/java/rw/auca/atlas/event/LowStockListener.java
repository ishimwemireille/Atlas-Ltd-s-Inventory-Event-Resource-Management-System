package rw.auca.atlas.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import rw.auca.atlas.model.Equipment;

/**
 * OBSERVER PATTERN: listens for LowStockEvent and logs a warning alert.
 * Registered as a Spring bean — automatically discovered by the ApplicationEventPublisher.
 */
@Component
public class LowStockListener {

  private static final Logger logger = LoggerFactory.getLogger(LowStockListener.class);

  /** OBSERVER PATTERN: reacts to low stock by logging a structured warning. */
  @EventListener
  public void handleLowStock(LowStockEvent event) {
    Equipment equipment = event.getEquipment();
    logger.warn(
        "LOW STOCK ALERT: {} — {} units remaining",
        equipment.getName(),
        equipment.getAvailableQuantity());
  }
}
