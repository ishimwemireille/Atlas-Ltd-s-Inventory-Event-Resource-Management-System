package rw.auca.atlas.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Represents an internal system user — credentials are issued by an Admin, no self-registration. */
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // validate that username is present and non-blank before persisting
  @NotBlank(message = "Username is required")
  @Column(nullable = false, unique = true)
  private String username;

  // validate email format — stored as unique constraint in DB
  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid address")
  @Column(nullable = false, unique = true)
  private String email;

  // write-only: excluded from JSON responses — never expose the hashed password
  @NotBlank(message = "Password is required")
  @JsonProperty(access = Access.WRITE_ONLY)
  @Column(nullable = false)
  private String password;

  // role must never be null — defaults to STAFF for ordinary accounts
  @NotNull(message = "Role is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role = UserRole.STAFF;

  public User() {}

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public UserRole getRole() { return role; }
  public void setRole(UserRole role) { this.role = role; }
}
