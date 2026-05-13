package rw.auca.atlas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.auca.atlas.exception.ResourceNotFoundException;
import rw.auca.atlas.model.Equipment;
import rw.auca.atlas.model.EquipmentStatus;
import rw.auca.atlas.repository.EquipmentRepository;

/**
 * Unit tests for {@link EquipmentService}.
 * Uses Mockito to isolate the service from the database (REPOSITORY PATTERN test).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentService")
class EquipmentServiceTest {

  @Mock
  private EquipmentRepository equipmentRepository;

  @InjectMocks
  private EquipmentService equipmentService;

  private Equipment sampleEquipment;

  @BeforeEach
  void setUp() {
    sampleEquipment = new Equipment();
    sampleEquipment.setName("Yamaha Mixer");
    sampleEquipment.setTotalQuantity(4);
    sampleEquipment.setAvailableQuantity(4);
    sampleEquipment.setStatus(EquipmentStatus.IN_STOCK);
  }

  @Test
  @DisplayName("findAll() returns all equipment from the repository")
  void findAll_returnsAllEquipment() {
    when(equipmentRepository.findAll()).thenReturn(List.of(sampleEquipment));

    List<Equipment> result = equipmentService.findAll();

    assertEquals(1, result.size());
    assertEquals("Yamaha Mixer", result.get(0).getName());
    verify(equipmentRepository).findAll();
  }

  @Test
  @DisplayName("findById() returns equipment when it exists")
  void findById_exists_returnsEquipment() {
    when(equipmentRepository.findById(1L)).thenReturn(Optional.of(sampleEquipment));

    Equipment result = equipmentService.findById(1L);

    assertEquals("Yamaha Mixer", result.getName());
  }

  @Test
  @DisplayName("findById() throws ResourceNotFoundException when equipment does not exist")
  void findById_notFound_throwsException() {
    when(equipmentRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> equipmentService.findById(99L));
  }

  @Test
  @DisplayName("save() delegates to repository and returns saved equipment")
  void save_persistsAndReturnsEquipment() {
    when(equipmentRepository.save(sampleEquipment)).thenReturn(sampleEquipment);

    Equipment result = equipmentService.save(sampleEquipment);

    assertEquals("Yamaha Mixer", result.getName());
    verify(equipmentRepository).save(sampleEquipment);
  }

  @Test
  @DisplayName("delete() calls repository deleteById when equipment exists")
  void delete_exists_deletesSuccessfully() {
    when(equipmentRepository.existsById(1L)).thenReturn(true);

    equipmentService.delete(1L);

    verify(equipmentRepository).deleteById(1L);
  }

  @Test
  @DisplayName("delete() throws ResourceNotFoundException when equipment does not exist")
  void delete_notFound_throwsException() {
    when(equipmentRepository.existsById(99L)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> equipmentService.delete(99L));
    verify(equipmentRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("findLowStock() returns equipment at or below the threshold")
  void findLowStock_returnsBelowThreshold() {
    Equipment lowStockItem = new Equipment();
    lowStockItem.setAvailableQuantity(1);
    when(equipmentRepository.findByAvailableQuantityLessThanEqual(2))
        .thenReturn(List.of(lowStockItem));

    List<Equipment> result = equipmentService.findLowStock();

    assertEquals(1, result.size());
    assertEquals(1, result.get(0).getAvailableQuantity());
  }
}
