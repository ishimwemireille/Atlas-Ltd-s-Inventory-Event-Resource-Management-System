package rw.auca.atlas.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** DTO for a single sale record in the admin sales report. */
public class SaleReportDTO {

  private Long id;
  private String equipmentName;
  private String categoryName;
  private int quantitySold;
  private LocalDate saleDate;
  private String buyerName;
  private String notes;
  private LocalDateTime recordedAt;

  public SaleReportDTO() {}

  public Long getId()                     { return id; }
  public void setId(Long id)             { this.id = id; }

  public String getEquipmentName()                 { return equipmentName; }
  public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

  public String getCategoryName()                  { return categoryName; }
  public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

  public int getQuantitySold()                   { return quantitySold; }
  public void setQuantitySold(int quantitySold)  { this.quantitySold = quantitySold; }

  public LocalDate getSaleDate()                 { return saleDate; }
  public void setSaleDate(LocalDate saleDate)    { this.saleDate = saleDate; }

  public String getBuyerName()                   { return buyerName; }
  public void setBuyerName(String buyerName)     { this.buyerName = buyerName; }

  public String getNotes()                       { return notes; }
  public void setNotes(String notes)             { this.notes = notes; }

  public LocalDateTime getRecordedAt()                  { return recordedAt; }
  public void setRecordedAt(LocalDateTime recordedAt)   { this.recordedAt = recordedAt; }
}
