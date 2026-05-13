package rw.auca.atlas.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import rw.auca.atlas.dto.AllocationReportDTO;
import rw.auca.atlas.dto.EquipmentReportDTO;
import rw.auca.atlas.dto.EventReportDTO;
import rw.auca.atlas.dto.ReportSummaryDTO;
import rw.auca.atlas.dto.SaleReportDTO;
import rw.auca.atlas.model.AllocationStatus;
import rw.auca.atlas.model.EquipmentAllocation;
import rw.auca.atlas.model.EquipmentStatus;
import rw.auca.atlas.model.EventStatus;
import rw.auca.atlas.repository.AllocationRepository;
import rw.auca.atlas.repository.EquipmentRepository;
import rw.auca.atlas.repository.EventRepository;
import rw.auca.atlas.repository.SaleRepository;
import rw.auca.atlas.repository.UserRepository;

/**
 * Service responsible for aggregating data across all modules to produce
 * admin-only reports: summary stats, equipment report, events report,
 * and full allocation history.
 */
@Service
public class ReportService {

  private final EquipmentRepository  equipmentRepository;
  private final EventRepository      eventRepository;
  private final AllocationRepository allocationRepository;
  private final UserRepository       userRepository;
  private final SaleRepository       saleRepository;

  public ReportService(EquipmentRepository equipmentRepository,
      EventRepository eventRepository,
      AllocationRepository allocationRepository,
      UserRepository userRepository,
      SaleRepository saleRepository) {
    this.equipmentRepository  = equipmentRepository;
    this.eventRepository      = eventRepository;
    this.allocationRepository = allocationRepository;
    this.userRepository       = userRepository;
    this.saleRepository       = saleRepository;
  }

  // ── Summary / Dashboard Stats ──────────────────────────────────────────────

  public ReportSummaryDTO getSummary() {
    ReportSummaryDTO dto = new ReportSummaryDTO();

    // Equipment stats
    var allEquipment = equipmentRepository.findAll();
    dto.setTotalEquipment(allEquipment.size());
    dto.setInStockCount(equipmentRepository.findByStatus(EquipmentStatus.IN_STOCK).size());
    dto.setReservedCount(equipmentRepository.findByStatus(EquipmentStatus.RESERVED).size());
    dto.setDeployedCount(equipmentRepository.findByStatus(EquipmentStatus.DEPLOYED).size());
    dto.setLowStockCount(equipmentRepository.findByAvailableQuantityLessThanEqual(2).size());
    dto.setTotalUnits(allEquipment.stream().mapToLong(e -> e.getTotalQuantity()).sum());
    dto.setAvailableUnits(allEquipment.stream().mapToLong(e -> e.getAvailableQuantity()).sum());

    // Event stats
    dto.setTotalEvents(eventRepository.count());
    dto.setPlannedEvents(eventRepository.findByStatus(EventStatus.PLANNED).size());
    dto.setInProgressEvents(eventRepository.findByStatus(EventStatus.IN_PROGRESS).size());
    dto.setCompletedEvents(eventRepository.findByStatus(EventStatus.COMPLETED).size());

    // Allocation stats
    dto.setTotalAllocations(allocationRepository.count());
    dto.setReservedAllocations(allocationRepository.countByAllocationStatus(AllocationStatus.RESERVED));
    dto.setDeployedAllocations(allocationRepository.countByAllocationStatus(AllocationStatus.DEPLOYED));
    dto.setReturnedAllocations(allocationRepository.countByAllocationStatus(AllocationStatus.RETURNED));

    // User stats
    dto.setTotalUsers(userRepository.count());

    // Sales stats
    var allSales = saleRepository.findAll();
    dto.setTotalSales(allSales.size());
    dto.setTotalUnitsSold(allSales.stream().mapToLong(s -> s.getQuantitySold()).sum());

    return dto;
  }

  // ── Equipment Report ───────────────────────────────────────────────────────

  public List<EquipmentReportDTO> getEquipmentReport() {
    return equipmentRepository.findAll().stream().map(eq -> {
      String catName = (eq.getCategory() != null) ? eq.getCategory().getName() : "—";
      return new EquipmentReportDTO(
          eq.getId(),
          eq.getName(),
          catName,
          eq.getTotalQuantity(),
          eq.getAvailableQuantity(),
          eq.getStatus().name()
      );
    }).collect(Collectors.toList());
  }

  // ── Events Report ──────────────────────────────────────────────────────────

  public List<EventReportDTO> getEventsReport() {
    return eventRepository.findAll().stream().map(event -> {
      List<EquipmentAllocation> allocs = allocationRepository.findByEventId(event.getId());

      EventReportDTO dto = new EventReportDTO();
      dto.setId(event.getId());
      dto.setName(event.getName());
      dto.setVenue(event.getVenue());
      dto.setEventDate(event.getEventDate());
      dto.setStatus(event.getStatus().name());
      dto.setTotalAllocations(allocs.size());
      dto.setTotalUnitsAllocated(allocs.stream().mapToInt(EquipmentAllocation::getQuantityAllocated).sum());
      dto.setDeployedAllocations((int) allocs.stream()
          .filter(a -> a.getAllocationStatus() == AllocationStatus.DEPLOYED).count());
      dto.setReturnedAllocations((int) allocs.stream()
          .filter(a -> a.getAllocationStatus() == AllocationStatus.RETURNED).count());
      return dto;
    }).collect(Collectors.toList());
  }

  // ── Allocations Report ─────────────────────────────────────────────────────

  public List<AllocationReportDTO> getAllocationsReport() {
    return allocationRepository.findAll().stream().map(alloc -> {
      AllocationReportDTO dto = new AllocationReportDTO();
      dto.setId(alloc.getId());
      dto.setEquipmentName(alloc.getEquipment().getName());
      dto.setEventName(alloc.getEvent().getName());
      dto.setEventVenue(alloc.getEvent().getVenue());
      dto.setQuantityAllocated(alloc.getQuantityAllocated());
      dto.setStatus(alloc.getAllocationStatus().name());
      return dto;
    }).collect(Collectors.toList());
  }

  // ── Sales Report ───────────────────────────────────────────────────────────

  public List<SaleReportDTO> getSalesReport() {
    return saleRepository.findAllByOrderBySaleDateDesc().stream().map(sale -> {
      SaleReportDTO dto = new SaleReportDTO();
      dto.setId(sale.getId());
      dto.setEquipmentName(sale.getEquipment().getName());
      String cat = (sale.getEquipment().getCategory() != null)
          ? sale.getEquipment().getCategory().getName() : "—";
      dto.setCategoryName(cat);
      dto.setQuantitySold(sale.getQuantitySold());
      dto.setSaleDate(sale.getSaleDate());
      dto.setBuyerName(sale.getBuyerName());
      dto.setNotes(sale.getNotes());
      dto.setRecordedAt(sale.getRecordedAt());
      return dto;
    }).collect(Collectors.toList());
  }
}
