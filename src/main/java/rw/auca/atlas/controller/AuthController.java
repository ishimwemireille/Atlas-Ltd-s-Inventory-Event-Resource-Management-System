package rw.auca.atlas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.atlas.config.JwtUtil;
import rw.auca.atlas.dto.AuthResponse;
import rw.auca.atlas.dto.LoginRequest;
import rw.auca.atlas.model.User;
import rw.auca.atlas.repository.UserRepository;
import rw.auca.atlas.service.AtlasUserDetailsService;

/** Handles authentication — login only. No self-registration; accounts are created by Admin. */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final AtlasUserDetailsService userDetailsService;
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  public AuthController(
      AuthenticationManager authenticationManager,
      AtlasUserDetailsService userDetailsService,
      JwtUtil jwtUtil,
      UserRepository userRepository) {
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
  }

  /**
   * Authenticates a user and returns a JWT token.
   * Credentials are issued internally by an Admin — there is no public registration.
   *
   * @param request login credentials
   * @return JWT token with username and role
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
    } catch (BadCredentialsException exception) {
      return ResponseEntity.status(401).body("Invalid username or password.");
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
    String token = jwtUtil.generateToken(userDetails);

    User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
    return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
  }
}
