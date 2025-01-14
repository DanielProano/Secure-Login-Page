import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Base64;

public class User {
    private String username;
    private String email;
    private String bio;
    private String friends;
    private String blocked;

    public User(String input) {
        String[] user = input.split(",");
        try {
            if (user.length == 3) {
                this.username = user[0];
                this.email = user[1];
                this.bio = user[2];
            } else {
                System.out.println("Error?");
            }
        } catch (Exception e) {
            System.out.println("Error");
        }
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String toString() {
        return String.format("%s,%s,%s", username, email, bio);
    }
}

