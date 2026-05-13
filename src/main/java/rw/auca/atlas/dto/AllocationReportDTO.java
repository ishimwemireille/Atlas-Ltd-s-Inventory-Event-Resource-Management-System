package rw.auca.atlas.dto;

/** One row in the Allocations report. */
public class AllocationReportDTO {

  private Long id;
  private String equipmentName;
  private String eventName;
  private String eventVenue;
  private int quantityAllocated;
  private String status;

  public AllocationReportDTO() {}

  public Long getId()                        { return id; }
  public void setId(Long id)                 { this.id = id; }

  public String getEquipmentName()           { return equipmentName; }
  public void setEquipmentName(String v)     { this.equipmentName = v; }

  public String getEventName()               { return eventName; }
  public void setEventName(String v)         { this.eventName = v; }

  public String getEventVenue()              { return eventVenue; }
  public void setEventVenue(String v)        { this.eventVenue = v; }

  public int getQuantityAllocated()          { return quantityAllocated; }
  public void setQuantityAllocated(int v)    { this.quantityAllocated = v; }

  public String getStatus()                  { return status; }
  public void setStatus(String status)       { this.status = status; }
}
