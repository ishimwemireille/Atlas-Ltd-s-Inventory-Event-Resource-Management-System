package rw.auca.atlas.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.atlas.model.AuditLog;
import rw.auca.atlas.repository.AuditLogRepository;

/**
 * Admin-only controller for viewing the system audit trail.
 * Every allocation, deployment, return, and sale action is recorded here.
 */
@RestController
@RequestMapping("/api/audit-logs")
// enforce admin-only access — audit data should not be visible to regular staff
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

  // REPOSITORY PATTERN: delegate all DB access through the repository interface
  private final AuditLogRepository auditLogRepository;

  // constructor injection — avoids field injection for testability and immutability
  public AuditLogController(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  /**
   * Returns the complete audit trail ordered by most recent action first.
   *
   * @return 200 OK with list of audit log entries
   */
  @GetMapping
  public ResponseEntity<List<AuditLog>> getAll() {
    // read-only endpoint — no mutation, so no @Valid or @Transactional needed here
    return ResponseEntity.ok(auditLogRepository.findAllByOrderByPerformedAtDesc());
  }
}
