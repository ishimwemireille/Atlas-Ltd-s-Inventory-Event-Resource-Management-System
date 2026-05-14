package rw.auca.atlas.model;

/** STATE PATTERN: defines the three lifecycle states of a single equipment allocation record. */
public enum AllocationStatus {
  // equipment has been reserved for the event but not yet dispatched
  RESERVED,
  // equipment has physically left for the event
  DEPLOYED,
  // equipment has been returned from the event
  RETURNED
}
