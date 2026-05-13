package rw.auca.atlas.dto;

import java.time.LocalDate;

/** One row in the Events report. */
public class EventReportDTO {

  private Long id;
  private String name;
  private String venue;
  private LocalDate eventDate;
  private String status;
  private int totalAllocations;
  private int totalUnitsAllocated;
  private int deployedAllocations;
  private int returnedAllocations;

  public EventReportDTO() {}

  public Long getId()                        { return id; }
  public void setId(Long id)                 { this.id = id; }

  public String getName()                    { return name; }
  public void setName(String name)           { this.name = name; }

  public String getVenue()                   { return venue; }
  public void setVenue(String venue)         { this.venue = venue; }

  public LocalDate getEventDate()            { return eventDate; }
  public void setEventDate(LocalDate d)      { this.eventDate = d; }

  public String getStatus()                  { return status; }
  public void setStatus(String status)       { this.status = status; }

  public int getTotalAllocations()           { return totalAllocations; }
  public void setTotalAllocations(int v)     { this.totalAllocations = v; }

  public int getTotalUnitsAllocated()        { return totalUnitsAllocated; }
  public void setTotalUnitsAllocated(int v)  { this.totalUnitsAllocated = v; }

  public int getDeployedAllocations()        { return deployedAllocations; }
  public void setDeployedAllocations(int v)  { this.deployedAllocations = v; }

  public int getReturnedAllocations()        { return returnedAllocations; }
  public void setReturnedAllocations(int v)  { this.returnedAllocations = v; }
}
