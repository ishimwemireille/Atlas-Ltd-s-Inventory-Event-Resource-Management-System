package rw.auca.atlas.dto;

/** Top-level dashboard statistics returned by GET /api/reports/summary. */
public class ReportSummaryDTO {

  // Equipment
  private long totalEquipment;
  private long inStockCount;
  private long reservedCount;
  private long deployedCount;
  private long lowStockCount;
  private long totalUnits;
  private long availableUnits;

  // Events
  private long totalEvents;
  private long plannedEvents;
  private long inProgressEvents;
  private long completedEvents;

  // Allocations
  private long totalAllocations;
  private long reservedAllocations;
  private long deployedAllocations;
  private long returnedAllocations;

  // Users
  private long totalUsers;

  // Sales
  private long totalSales;
  private long totalUnitsSold;

  public ReportSummaryDTO() {}

  // ── Getters & Setters ──────────────────────────────────────────────────────

  public long getTotalEquipment()        { return totalEquipment; }
  public void setTotalEquipment(long v)  { this.totalEquipment = v; }

  public long getInStockCount()          { return inStockCount; }
  public void setInStockCount(long v)    { this.inStockCount = v; }

  public long getReservedCount()         { return reservedCount; }
  public void setReservedCount(long v)   { this.reservedCount = v; }

  public long getDeployedCount()         { return deployedCount; }
  public void setDeployedCount(long v)   { this.deployedCount = v; }

  public long getLowStockCount()         { return lowStockCount; }
  public void setLowStockCount(long v)   { this.lowStockCount = v; }

  public long getTotalUnits()            { return totalUnits; }
  public void setTotalUnits(long v)      { this.totalUnits = v; }

  public long getAvailableUnits()        { return availableUnits; }
  public void setAvailableUnits(long v)  { this.availableUnits = v; }

  public long getTotalEvents()           { return totalEvents; }
  public void setTotalEvents(long v)     { this.totalEvents = v; }

  public long getPlannedEvents()         { return plannedEvents; }
  public void setPlannedEvents(long v)   { this.plannedEvents = v; }

  public long getInProgressEvents()      { return inProgressEvents; }
  public void setInProgressEvents(long v){ this.inProgressEvents = v; }

  public long getCompletedEvents()       { return completedEvents; }
  public void setCompletedEvents(long v) { this.completedEvents = v; }

  public long getTotalAllocations()          { return totalAllocations; }
  public void setTotalAllocations(long v)    { this.totalAllocations = v; }

  public long getReservedAllocations()       { return reservedAllocations; }
  public void setReservedAllocations(long v) { this.reservedAllocations = v; }

  public long getDeployedAllocations()       { return deployedAllocations; }
  public void setDeployedAllocations(long v) { this.deployedAllocations = v; }

  public long getReturnedAllocations()       { return returnedAllocations; }
  public void setReturnedAllocations(long v) { this.returnedAllocations = v; }

  public long getTotalUsers()            { return totalUsers; }
  public void setTotalUsers(long v)      { this.totalUsers = v; }

  public long getTotalSales()            { return totalSales; }
  public void setTotalSales(long v)      { this.totalSales = v; }

  public long getTotalUnitsSold()        { return totalUnitsSold; }
  public void setTotalUnitsSold(long v)  { this.totalUnitsSold = v; }
}
