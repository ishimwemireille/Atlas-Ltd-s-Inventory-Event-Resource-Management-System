package rw.auca.atlas.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.atlas.dto.AllocationReportDTO;
import rw.auca.atlas.dto.EquipmentReportDTO;
import rw.auca.atlas.dto.EventReportDTO;
import rw.auca.atlas.dto.ReportSummaryDTO;
import rw.auca.atlas.service.ReportService;

/**
 * REST controller for admin-only reporting endpoints.
 *
 * All endpoints require the ADMIN role — enforced via @PreAuthorize
 * using the @EnableMethodSecurity annotation on SecurityConfig.
 *
 * Endpoints:
 *   GET /api/reports/summary     — aggregated statistics
 *   GET /api/reports/equipment   — full equipment report
 *   GET /api/reports/events      — events with allocation counts
 *   GET /api/reports/allocations — complete allocation history
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  /** Overall system statistics — used for report summary cards. */
  @GetMapping("/summary")
  public ResponseEntity<ReportSummaryDTO> getSummary() {
    return ResponseEntity.ok(reportService.getSummary());
  }

  /** Full equipment list with status, quantities, and low-stock flag. */
  @GetMapping("/equipment")
  public ResponseEntity<List<EquipmentReportDTO>> getEquipmentReport() {
    return ResponseEntity.ok(reportService.getEquipmentReport());
  }

  /** All events with their allocation counts and unit totals. */
  @GetMapping("/events")
  public ResponseEntity<List<EventReportDTO>> getEventsReport() {
    return ResponseEntity.ok(reportService.getEventsReport());
  }

  /** Complete allocation history across all events. */
  @GetMapping("/allocations")
  public ResponseEntity<List<AllocationReportDTO>> getAllocationsReport() {
    return ResponseEntity.ok(reportService.getAllocationsReport());
  }
}
