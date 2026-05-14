package rw.auca.atlas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.atlas.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
  List<AuditLog> findAllByOrderByPerformedAtDesc();
}
