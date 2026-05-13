package rw.auca.atlas.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.atlas.exception.ResourceNotFoundException;
import rw.auca.atlas.model.Event;
import rw.auca.atlas.repository.EventRepository;

/** Service layer for Event CRUD operations. */
@Service
@Transactional
public class EventService {

  // REPOSITORY PATTERN: data access abstraction
  private final EventRepository eventRepository;

  public EventService(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  /**
   * Returns all events.
   *
   * @return list of all events
   */
  public List<Event> findAll() {
    return eventRepository.findAll();
  }

  /**
   * Finds an event by its ID.
   *
   * @param id the event ID
   * @return the matching event
   * @throws ResourceNotFoundException if no event with that ID exists
   */
  public Event findById(Long id) {
    return eventRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
  }

  /**
   * Persists a new event.
   *
   * @param event the event to save
   * @return the saved event with generated ID
   */
  public Event save(Event event) {
    return eventRepository.save(event);
  }

  /**
   * Updates an existing event record.
   *
   * @param id the ID of the event to update
   * @param updated the new field values
   * @return the updated event
   * @throws ResourceNotFoundException if no event with that ID exists
   */
  public Event update(Long id, Event updated) {
    Event existing = findById(id);
    existing.setName(updated.getName());
    existing.setVenue(updated.getVenue());
    existing.setEventDate(updated.getEventDate());
    existing.setDescription(updated.getDescription());
    existing.setStatus(updated.getStatus());
    return eventRepository.save(existing);
  }

  /**
   * Deletes an event by its ID.
   *
   * @param id the ID of the event to delete
   * @throws ResourceNotFoundException if no event with that ID exists
   */
  public void delete(Long id) {
    if (!eventRepository.existsById(id)) {
      throw new ResourceNotFoundException("Event not found with id: " + id);
    }
    eventRepository.deleteById(id);
  }
}
