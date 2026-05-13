package rw.auca.atlas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Records a sale transaction — equipment permanently removed from inventory. */
@Entity
@Table(name = "equipment_sales")
public class EquipmentSale {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Equipment is required")
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "equipment_id", nullable = false)
  @JsonIgnoreProperties("allocations")
  private Equipment equipment;

  @Min(value = 1, message = "Quantity sold must be at least 1")
  @Column(nullable = false)
  private int quantitySold;

  @NotNull(message = "Sale date is required")
  @Column(nullable = false)
  private LocalDate saleDate;

  private String buyerName;
  private String notes;

  @Column(nullable = false, updatable = false)
  private LocalDateTime recordedAt;

  @PrePersist
  protected void onCreate() {
    recordedAt = LocalDateTime.now();
  }

  public EquipmentSale() {}

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Equipment getEquipment() { return equipment; }
  public void setEquipment(Equipment equipment) { this.equipment = equipment; }

  public int getQuantitySold() { return quantitySold; }
  public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }

  public LocalDate getSaleDate() { return saleDate; }
  public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

  public String getBuyerName() { return buyerName; }
  public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }

  public LocalDateTime getRecordedAt() { return recordedAt; }
}
