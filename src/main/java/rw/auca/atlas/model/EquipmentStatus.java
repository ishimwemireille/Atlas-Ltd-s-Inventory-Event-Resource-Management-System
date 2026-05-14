package rw.auca.atlas.model;

/** STATE PATTERN: defines the four lifecycle states of a piece of equipment. */
public enum EquipmentStatus {
  // equipment is available for reservation
  IN_STOCK,
  // all units are reserved for upcoming events — none available
  RESERVED,
  // equipment has physically left the warehouse for an event
  DEPLOYED,
  // equipment has been returned after an event
  RETURNED
}
