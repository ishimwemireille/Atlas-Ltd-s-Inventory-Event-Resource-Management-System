package rw.auca.atlas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Records every significant action performed in the system with who did it and when. */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // action label — e.g. DEPLOY, RETURN, SALE, CREATE, UPDATE, DELETE
  @Column(nullable = false)
  private String action;

  // entity class name that was acted upon — e.g. Equipment, Event, Allocation, Sale
  @Column(nullable = false)
  private String entityType;

  // primary key of the entity — allows drilling down to the specific record
  private Long entityId;

  // human-readable summary written by the service layer
  @Column(length = 500)
  private String description;

  // resolve the current logged-in user from JWT security context at write time
  @Column(nullable = false)
  private String performedBy;

  // auto-set timestamp via @PrePersist — not settable by client
  @Column(nullable = false, updatable = false)
  private LocalDateTime performedAt;

  @PrePersist
  protected void onCreate() { performedAt = LocalDateTime.now(); }

  public AuditLog() {}

  public AuditLog(String action, String entityType, Long entityId, String description, String performedBy) {
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.description = description;
    this.performedBy = performedBy;
  }

  public Long getId()                          { return id; }
  public String getAction()                    { return action; }
  public void setAction(String action)         { this.action = action; }
  public String getEntityType()                { return entityType; }
  public void setEntityType(String v)          { this.entityType = v; }
  public Long getEntityId()                    { return entityId; }
  public void setEntityId(Long v)              { this.entityId = v; }
  public String getDescription()               { return description; }
  public void setDescription(String v)         { this.description = v; }
  public String getPerformedBy()               { return performedBy; }
  public void setPerformedBy(String v)         { this.performedBy = v; }
  public LocalDateTime getPerformedAt()        { return performedAt; }
}
