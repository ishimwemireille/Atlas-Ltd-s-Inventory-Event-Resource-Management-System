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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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

  // REPOSITORY PATTERN: equipment reference resolved via FK — never embedded directly
  @NotNull(message = "Equipment is required")
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "equipment_id", nullable = false)
  private Equipment equipment;

  // suppress allocations list on Event to prevent circular JSON serialization
  @NotNull(message = "Event is required")
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "event_id", nullable = false)
  @JsonIgnoreProperties("allocations")
  private Event event;

  // at least 1 unit must be allocated — 0 allocations are meaningless
  @Min(value = 1, message = "Quantity allocated must be at least 1")
  @Column(nullable = false)
  private int quantityAllocated;

  // STATE PATTERN: tracks which lifecycle state this allocation is in
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AllocationStatus allocationStatus = AllocationStatus.RESERVED;

  // rental price recorded at allocation time — may differ from current selling price
  @Column(precision = 12, scale = 2)
  private BigDecimal rentalPricePerUnit;

  // timestamps are set programmatically by the service — never by the client
  private LocalDateTime deployedAt;
  private LocalDateTime returnedAt;

  // return condition captured when equipment is brought back from an event
  @Enumerated(EnumType.STRING)
  private ReturnCondition returnCondition;

  private String damageNotes;

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

  public BigDecimal getRentalPricePerUnit() { return rentalPricePerUnit; }
  public void setRentalPricePerUnit(BigDecimal rentalPricePerUnit) { this.rentalPricePerUnit = rentalPricePerUnit; }

  public LocalDateTime getDeployedAt() { return deployedAt; }
  public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }

  public LocalDateTime getReturnedAt() { return returnedAt; }
  public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

  public ReturnCondition getReturnCondition() { return returnCondition; }
  public void setReturnCondition(ReturnCondition returnCondition) { this.returnCondition = returnCondition; }

  public String getDamageNotes() { return damageNotes; }
  public void setDamageNotes(String damageNotes) { this.damageNotes = damageNotes; }
}
