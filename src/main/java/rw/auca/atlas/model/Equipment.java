package rw.auca.atlas.model;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Represents a piece of equipment owned by Atlas Turbo LTD.
 *
 * <p>Implements the STATE PATTERN — equipment transitions through four states:
 * IN_STOCK → RESERVED → DEPLOYED → IN_STOCK (via returnStock).
 * State transitions are enforced exclusively through the methods below.
 */
@Entity
@Table(name = "equipment")
public class Equipment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Equipment name is required")
  @Column(nullable = false)
  private String name;

  private String description;

  @NotNull(message = "Category is required")
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "category_id")
  private Category category;

  @Min(value = 1, message = "Total quantity must be at least 1")
  @Column(nullable = false)
  private int totalQuantity;

  @Min(value = 0, message = "Available quantity cannot be negative")
  @Column(nullable = false)
  private int availableQuantity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EquipmentStatus status = EquipmentStatus.IN_STOCK;

  @Column(updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  public Equipment() {}

  // ── Getters & Setters ──────────────────────────────────────────────────────

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public Category getCategory() { return category; }
  public void setCategory(Category category) { this.category = category; }

  public int getTotalQuantity() { return totalQuantity; }
  public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

  public int getAvailableQuantity() { return availableQuantity; }
  public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

  public EquipmentStatus getStatus() { return status; }
  public void setStatus(EquipmentStatus status) { this.status = status; }

  public LocalDateTime getCreatedAt() { return createdAt; }

  // ── State Pattern methods ──────────────────────────────────────────────────

  /**
   * STATE PATTERN: transition from IN_STOCK to RESERVED.
   * Reduces availableQuantity by qty. Sets status to RESERVED when stock hits zero.
   *
   * @param qty the number of units to reserve
   * @throws IllegalStateException if available stock is insufficient
   */
  public void reserve(int qty) {
    if (availableQuantity < qty) {
      throw new IllegalStateException(
          "Insufficient stock: requested " + qty + ", available " + availableQuantity);
    }
    availableQuantity -= qty;
    status = (availableQuantity == 0) ? EquipmentStatus.RESERVED : EquipmentStatus.IN_STOCK;
  }

  /**
   * STATE PATTERN: transition from RESERVED to DEPLOYED.
   * Called when equipment physically leaves for an event.
   */
  public void deploy() {
    status = EquipmentStatus.DEPLOYED;
  }

  /**
   * STATE PATTERN: transition from DEPLOYED back to IN_STOCK.
   * Restores availableQuantity after equipment is returned from an event.
   *
   * @param qty the number of units being returned
   */
  public void returnStock(int qty) {
    availableQuantity += qty;
    status = EquipmentStatus.IN_STOCK;
  }
}
