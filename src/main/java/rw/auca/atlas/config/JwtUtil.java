package rw.auca.atlas.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/** Utility class for generating and validating JWT tokens. */
@Component
public class JwtUtil {

  @Value("${atlas.jwt.secret}")
  private String secret;

  @Value("${atlas.jwt.expiration}")
  private long expiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Generates a JWT token for an authenticated user.
   *
   * @param userDetails the authenticated user
   * @return signed JWT token string
   */
  public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
        .subject(userDetails.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Extracts the username claim from a JWT token.
   *
   * @param token the JWT string
   * @return the username embedded in the token
   */
  public String extractUsername(String token) {
    return getClaims(token).getSubject();
  }

  /**
   * Validates a token against the provided user details.
   *
   * @param token the JWT string
   * @param userDetails the user to validate against
   * @return true if the token is valid and not expired
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return getClaims(token).getExpiration().before(new Date());
  }

  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
