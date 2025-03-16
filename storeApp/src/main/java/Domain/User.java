package Domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class User extends AbstractEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public User(String firstName, String lastName, String email, String password) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String toString() {
        return "User {" +
                "id=" + id +
                ", first name='" + firstName + '\'' +
                ", last name='" + lastName + '\'' +
                ", email=" + email +
                ", password=" + password +
                '}';
    }

    public String toCsv() {
        return getId() + "-" + firstName + "-" + lastName + "-" + email + "-" + password;
    }

    public static User fromCsv(String csv) {
        String[] parts = csv.split("-");
        int id = Integer.parseInt(parts[0]);
        String firstName = parts[1];
        String lastName = parts[2];
        String email = parts[3];
        String password = parts[4];
        User user = new User(firstName,lastName,email,password);
        user.id=id;
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
