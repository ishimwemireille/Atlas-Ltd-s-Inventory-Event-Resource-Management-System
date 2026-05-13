package rw.auca.atlas.dto;

/** Response body for a successful login — carries the JWT and user metadata. */
public class AuthResponse {

  private final String token;
  private final String username;
  private final String role;

  public AuthResponse(String token, String username, String role) {
    this.token = token;
    this.username = username;
    this.role = role;
  }

  public String getToken() {
    return token;
  }

  public String getUsername() {
    return username;
  }

  public String getRole() {
    return role;
  }
}
