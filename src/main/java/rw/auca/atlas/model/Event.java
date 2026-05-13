package rw.auca.atlas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Represents a client event (concert, wedding, corporate) managed by Atlas Turbo LTD. */
@Entity
@Table(name = "events")
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Event name is required")
  @Column(nullable = false)
  private String name;

  @NotBlank(message = "Venue is required")
  @Column(nullable = false)
  private String venue;

  @NotNull(message = "Event date is required")
  @FutureOrPresent(message = "Event date cannot be in the past")
  @Column(nullable = false)
  private LocalDate eventDate;

  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventStatus status = EventStatus.PLANNED;

  // Excluded from JSON — allocations are fetched separately via GET /api/allocations/event/{id}
  @JsonIgnore
  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EquipmentAllocation> allocations = new ArrayList<>();

  public Event() {}

  // ── Getters & Setters ──────────────────────────────────────────────────────

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getVenue() { return venue; }
  public void setVenue(String venue) { this.venue = venue; }

  public LocalDate getEventDate() { return eventDate; }
  public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public EventStatus getStatus() { return status; }
  public void setStatus(EventStatus status) { this.status = status; }

  public List<EquipmentAllocation> getAllocations() { return allocations; }
  public void setAllocations(List<EquipmentAllocation> allocations) { this.allocations = allocations; }
}
