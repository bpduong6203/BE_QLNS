// src/main/java/DA/backend/payload/LoginRequest.java
package DA.backend.payload;

public class LoginRequest {
    private String identifier; // Can be email or ID
    private String password;

    // Getters and Setters
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
