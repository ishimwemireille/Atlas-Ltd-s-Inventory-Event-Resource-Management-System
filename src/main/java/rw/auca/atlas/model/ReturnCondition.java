package rw.auca.atlas.model;

/** Describes the physical condition of equipment when it is returned from an event. */
public enum ReturnCondition {
  // equipment returned without any damage
  GOOD,
  // equipment returned with damage — see damageNotes on the allocation
  DAMAGED,
  // some components are missing — see damageNotes for details
  MISSING_PARTS
}
