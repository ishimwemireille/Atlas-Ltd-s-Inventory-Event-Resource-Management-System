package rw.auca.atlas.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.atlas.model.Event;
import rw.auca.atlas.model.EventStatus;

// REPOSITORY PATTERN: data access abstraction for Event
public interface EventRepository extends JpaRepository<Event, Long> {

  List<Event> findByStatus(EventStatus status);
}
