package model.dbrequest;

/**
 *
 * @author adotal
 */
public class RecoverPasswordRequest {
    // Identificador del tipo de solicitud
    private String type = "RECOVER_PASSWORD";
    private String email;
    private String password;

    // Jackson necesita un constructor vacío
    public RecoverPasswordRequest() {}

    public RecoverPasswordRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
