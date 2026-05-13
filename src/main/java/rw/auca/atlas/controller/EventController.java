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

  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @GetMapping
  public ResponseEntity<List<Event>> getAllEvents() {
    return ResponseEntity.ok(eventService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Event> getEventById(@PathVariable Long id) {
    return ResponseEntity.ok(eventService.findById(id));
  }

  @PostMapping
  public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
    return ResponseEntity.status(HttpStatus.CREATED).body(eventService.save(event));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Event> updateEvent(
      @PathVariable Long id, @Valid @RequestBody Event event) {
    return ResponseEntity.ok(eventService.update(id, event));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
    eventService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
