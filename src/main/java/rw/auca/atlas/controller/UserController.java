package rw.auca.atlas.controller;

import java.util.List;
import jakarta.validation.Valid;
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
// enforce admin-only access at the class level — no staff can call any of these endpoints
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

  // REPOSITORY PATTERN: delegate DB access through the repository interface
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // constructor injection — avoids field injection for testability and immutability
  public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Returns all registered users. Admin-only.
   *
   * @return 200 OK with list of users
   */
  @GetMapping
  public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userRepository.findAll());
  }

  /**
   * Creates a new user account. Password is BCrypt-hashed before saving.
   *
   * @param user the new user data from the request body
   * @return 201 Created with the saved user
   */
  @PostMapping
  public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
    // validate input before hitting the database — rejects blank username/email/password
    // hash password with BCrypt before persisting — never store plain text
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
  }

  /**
   * Updates an existing user's profile. Only non-blank fields are applied.
   * Password is re-hashed only if a new one is provided.
   *
   * @param id      the ID of the user to update
   * @param updates the fields to update
   * @return 200 OK with the updated user, or 404 if not found
   */
  @PutMapping("/{id}")
  public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updates) {
    // load existing user — throws 404 if not found
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

    // only apply non-blank fields — prevents accidental data loss on partial updates
    if (updates.getUsername() != null && !updates.getUsername().isBlank()) {
      user.setUsername(updates.getUsername());
    }
    if (updates.getEmail() != null && !updates.getEmail().isBlank()) {
      user.setEmail(updates.getEmail());
    }
    if (updates.getRole() != null) {
      user.setRole(updates.getRole());
    }
    if (updates.getPassword() != null && !updates.getPassword().isBlank()) {
      // hash password with BCrypt before persisting — never store plain text
      user.setPassword(passwordEncoder.encode(updates.getPassword()));
    }
    return ResponseEntity.ok(userRepository.save(user));
  }

  /**
   * Deletes a user account by ID.
   *
   * @param id the ID of the user to delete
   * @return 204 No Content on success, or 404 if not found
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    // validate input before hitting the database — fail fast if user does not exist
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User not found with id " + id);
    }
    userRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
