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

  // validate event name is present before persisting
  @NotBlank(message = "Event name is required")
  @Column(nullable = false)
  private String name;

  // venue is required — events must have a physical location
  @NotBlank(message = "Venue is required")
  @Column(nullable = false)
  private String venue;

  // event date is mandatory — used for scheduling and calendar view
  @NotNull(message = "Event date is required")
  @Column(nullable = false)
  private LocalDate eventDate;

  private String description;

  // client details are optional — recorded for contact and billing purposes
  private String clientName;
  private String clientPhone;
  private String clientEmail;

  // default status is PLANNED when an event is first created
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventStatus status = EventStatus.PLANNED;

  // excluded from JSON — allocations are fetched separately via GET /api/allocations/event/{id}
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

  public String getClientName() { return clientName; }
  public void setClientName(String clientName) { this.clientName = clientName; }

  public String getClientPhone() { return clientPhone; }
  public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

  public String getClientEmail() { return clientEmail; }
  public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

  public EventStatus getStatus() { return status; }
  public void setStatus(EventStatus status) { this.status = status; }

  public List<EquipmentAllocation> getAllocations() { return allocations; }
  public void setAllocations(List<EquipmentAllocation> allocations) { this.allocations = allocations; }
}
