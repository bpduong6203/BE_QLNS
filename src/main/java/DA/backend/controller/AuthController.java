// src/main/java/DA/backend/controller/AuthController.java
package DA.backend.controller;

import DA.backend.payload.LoginRequest;
import DA.backend.util.JwtUtil;
import DA.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        String identifier = loginRequest.getIdentifier();
        String password = loginRequest.getPassword();

        // Create authentication token
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                identifier,
                password
        );

        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtUtil.generateToken(authentication);

            // Retrieve user role
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String role = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ADMIN"); // Default role without 'ROLE_' prefix

            // Optionally, retrieve the user's ID
            String userId = userPrincipal.getId(); // Assuming getId() returns String

            // Prepare response
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", jwt);
            response.put("tokenType", "Bearer");
            response.put("role", role); // e.g., "ADMIN"
            response.put("userId", userId); // Include user ID in response

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(500).body("Authentication failed");
        }
    }

}
