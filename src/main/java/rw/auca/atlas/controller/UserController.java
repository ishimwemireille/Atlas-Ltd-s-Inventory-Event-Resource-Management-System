package rw.auca.atlas.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.atlas.exception.ResourceNotFoundException;
import rw.auca.atlas.model.User;
import rw.auca.atlas.repository.UserRepository;

/**
 * Admin-only controller for managing internal user accounts.
 * Only Admins can create accounts — staff have no self-registration capability.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping
  public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userRepository.findAll());
  }

  @PostMapping
  public ResponseEntity<User> createUser(@RequestBody User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
  }

  @PutMapping("/{id}")
  public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updates) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    if (updates.getUsername() != null && !updates.getUsername().isBlank()) {
      user.setUsername(updates.getUsername());
    }
    if (updates.getEmail() != null && !updates.getEmail().isBlank()) {
      user.setEmail(updates.getEmail());
    }
    if (updates.getRole() != null) {
      user.setRole(updates.getRole());
    }
    // Only update password if a new one is provided
    if (updates.getPassword() != null && !updates.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(updates.getPassword()));
    }
    return ResponseEntity.ok(userRepository.save(user));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User not found with id " + id);
    }
    userRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
