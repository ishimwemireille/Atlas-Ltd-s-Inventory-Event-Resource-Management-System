package rw.auca.atlas.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rw.auca.atlas.model.User;
import rw.auca.atlas.model.UserRole;
import rw.auca.atlas.repository.UserRepository;

/**
 * Seeds the default Admin and Staff accounts on first startup.
 * Accounts are internal — no self-registration exists in this system.
 *
 * Default credentials:
 *   Admin  → username: admin   password: admin123
 *   Staff  → username: staff   password: staff123
 */
@Component
public class DataInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (userRepository.findByUsername("admin").isEmpty()) {
      User admin = new User();
      admin.setUsername("admin");
      admin.setEmail("admin@atlasturbo.rw");
      admin.setPassword(passwordEncoder.encode("admin123"));
      admin.setRole(UserRole.ADMIN);
      userRepository.save(admin);
    }

    if (userRepository.findByUsername("staff").isEmpty()) {
      User staff = new User();
      staff.setUsername("staff");
      staff.setEmail("staff@atlasturbo.rw");
      staff.setPassword(passwordEncoder.encode("staff123"));
      staff.setRole(UserRole.STAFF);
      userRepository.save(staff);
    }
  }
}
