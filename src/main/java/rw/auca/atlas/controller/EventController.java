package rw.auca.atlas.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.atlas.model.Event;
import rw.auca.atlas.service.EventService;

/** REST controller exposing event management endpoints. */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

  // constructor injection — avoids field injection for testability and immutability
  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  /**
   * Returns all events in the system.
   *
   * @return 200 OK with list of events
   */
  @GetMapping
  public ResponseEntity<List<Event>> getAllEvents() {
    return ResponseEntity.ok(eventService.findAll());
  }

  /**
   * Returns a single event by ID.
   *
   * @param id the event ID
   * @return 200 OK with the event, or 404 if not found
   */
  @GetMapping("/{id}")
  public ResponseEntity<Event> getEventById(@PathVariable Long id) {
    return ResponseEntity.ok(eventService.findById(id));
  }

  /**
   * Creates a new event. Input is validated via {@code @Valid} before persistence.
   *
   * @param event the event data from the request body
   * @return 201 Created with the saved event
   */
  @PostMapping
  public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
    // @Valid triggers bean-validation — rejects request if @NotBlank/@NotNull constraints fail
    return ResponseEntity.status(HttpStatus.CREATED).body(eventService.save(event));
  }

  /**
   * Updates an existing event record.
   *
   * @param id    the ID of the event to update
   * @param event the updated event data
   * @return 200 OK with the updated event, or 404 if not found
   */
  @PutMapping("/{id}")
  public ResponseEntity<Event> updateEvent(
      @PathVariable Long id, @Valid @RequestBody Event event) {
    // @Valid ensures updated data still satisfies all validation constraints
    return ResponseEntity.ok(eventService.update(id, event));
  }

  /**
   * Deletes an event by ID.
   *
   * @param id the ID of the event to delete
   * @return 204 No Content on success, or 404 if not found
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
    eventService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
