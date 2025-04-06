// src/main/java/DA/backend/service/CustomUserDetailsService.java
package DA.backend.service;

import DA.backend.entity.User;
import DA.backend.repository.UserRepository;
import DA.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by username (which can be email or ID).
     *
     * @param identifier The username or ID.
     * @return UserDetails object.
     * @throws UsernameNotFoundException if user not found.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user;

        if (isValidEmail(identifier)) {
            // Load by email
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found with email: " + identifier)
                    );
        } else {
            // Load by ID
            user = userRepository.findById(identifier)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("User not found with ID: " + identifier)
                    );
        }

        return UserPrincipal.create(user);
    }

    /**
     * Utility method to check if a string is a valid email.
     *
     * @param identifier The string to check.
     * @return True if valid email, else false.
     */
    private boolean isValidEmail(String identifier) {
        // Simple regex for email validation
        return StringUtils.hasText(identifier) && identifier.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
