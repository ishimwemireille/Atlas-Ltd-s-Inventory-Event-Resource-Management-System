package rw.auca.atlas.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.atlas.model.AuditLog;
import rw.auca.atlas.repository.AuditLogRepository;

/**
 * Centralised service for writing audit log entries.
 * Single responsibility — only handles audit persistence.
 */
@Service
public class AuditLogService {

  // REPOSITORY PATTERN: delegate all DB access through JPA repository interface
  private final AuditLogRepository auditLogRepository;

  // constructor injection — avoids field injection for testability and immutability
  public AuditLogService(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  /**
   * Persists an audit log entry for a business action.
   * Automatically resolves the performing user from the active security context.
   *
   * @param action      the action label, e.g. "DEPLOY", "RETURN", "SALE"
   * @param entityType  the entity being acted on, e.g. "Allocation", "Sale"
   * @param entityId    the primary key of the entity
   * @param description a human-readable description of the action
   */
  @Transactional
  public void log(String action, String entityType, Long entityId, String description) {
    // resolve the current logged-in user from JWT security context
    String username = "system";
    try {
      var auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated()) {
        username = auth.getName();
      }
    } catch (Exception ignored) {
      // fall back to "system" if no authenticated context is available (e.g. startup events)
    }

    // REPOSITORY PATTERN: delegate audit log persistence through the repository
    auditLogRepository.save(new AuditLog(action, entityType, entityId, description, username));
  }
}
