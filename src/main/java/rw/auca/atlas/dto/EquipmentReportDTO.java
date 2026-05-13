package rw.auca.atlas.dto;

/** One row in the Equipment report. */
public class EquipmentReportDTO {

  private Long id;
  private String name;
  private String categoryName;
  private int totalQuantity;
  private int availableQuantity;
  private int allocatedQuantity;
  private String status;
  private boolean lowStock;

  public EquipmentReportDTO() {}

  public EquipmentReportDTO(Long id, String name, String categoryName,
      int totalQuantity, int availableQuantity, String status) {
    this.id = id;
    this.name = name;
    this.categoryName = categoryName;
    this.totalQuantity = totalQuantity;
    this.availableQuantity = availableQuantity;
    this.allocatedQuantity = totalQuantity - availableQuantity;
    this.status = status;
    this.lowStock = availableQuantity <= 2;
  }

  public Long getId()                  { return id; }
  public void setId(Long id)           { this.id = id; }

  public String getName()              { return name; }
  public void setName(String name)     { this.name = name; }

  public String getCategoryName()              { return categoryName; }
  public void setCategoryName(String v)        { this.categoryName = v; }

  public int getTotalQuantity()                { return totalQuantity; }
  public void setTotalQuantity(int v)          { this.totalQuantity = v; }

  public int getAvailableQuantity()            { return availableQuantity; }
  public void setAvailableQuantity(int v)      { this.availableQuantity = v; }

  public int getAllocatedQuantity()             { return allocatedQuantity; }
  public void setAllocatedQuantity(int v)      { this.allocatedQuantity = v; }

  public String getStatus()                    { return status; }
  public void setStatus(String status)         { this.status = status; }

  public boolean isLowStock()                  { return lowStock; }
  public void setLowStock(boolean lowStock)    { this.lowStock = lowStock; }
}
