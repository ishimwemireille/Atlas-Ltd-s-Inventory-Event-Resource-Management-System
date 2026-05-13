package rw.auca.atlas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Association class linking Equipment to an Event for a given quantity.
 * Tracks the full allocation lifecycle: RESERVED → DEPLOYED → RETURNED.
 */
@Entity
@Table(name = "equipment_allocations")
public class EquipmentAllocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "equipment_id", nullable = false)
  private Equipment equipment;

  // Suppress allocations list on Event to prevent circular JSON serialization
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "event_id", nullable = false)
  @JsonIgnoreProperties("allocations")
  private Event event;

  @Column(nullable = false)
  private int quantityAllocated;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AllocationStatus allocationStatus = AllocationStatus.RESERVED;

  private LocalDateTime deployedAt;
  private LocalDateTime returnedAt;

  public EquipmentAllocation() {}

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Equipment getEquipment() { return equipment; }
  public void setEquipment(Equipment equipment) { this.equipment = equipment; }

  public Event getEvent() { return event; }
  public void setEvent(Event event) { this.event = event; }

  public int getQuantityAllocated() { return quantityAllocated; }
  public void setQuantityAllocated(int quantityAllocated) { this.quantityAllocated = quantityAllocated; }

  public AllocationStatus getAllocationStatus() { return allocationStatus; }
  public void setAllocationStatus(AllocationStatus allocationStatus) { this.allocationStatus = allocationStatus; }

  public LocalDateTime getDeployedAt() { return deployedAt; }
  public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }

  public LocalDateTime getReturnedAt() { return returnedAt; }
  public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
}
