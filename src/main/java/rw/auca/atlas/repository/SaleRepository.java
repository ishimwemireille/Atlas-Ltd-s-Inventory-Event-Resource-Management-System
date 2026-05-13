package rw.auca.atlas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.atlas.model.EquipmentSale;

public interface SaleRepository extends JpaRepository<EquipmentSale, Long> {
  List<EquipmentSale> findAllByOrderBySaleDateDesc();
}
