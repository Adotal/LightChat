package model.dbrequest;

/**
 *
 * @author adotal
 */
public class SignUpRequest {

    private String type = "SIGNUP"; // Identificador del tipo de solicitud

    // Información de usuario
    private String name;
    private String email;    
    private String password;
    private String state = "disconnected";

    // Jackson necesita un constructor vacío
    public SignUpRequest() {        
    }

    public SignUpRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }        

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
