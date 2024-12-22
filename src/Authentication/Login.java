package Authentication;

import Connection.DatabaseConnection;

import java.io.Console;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class Login {
    private final Scanner scanner;

    public Login(Scanner scanner) {
        this.scanner = scanner;
    }

    // Sign In Method - Verifies User from User Table
    public boolean signIn() {
        System.out.println("\n===== 🔑 Sign In =====");

        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM User WHERE LOWER(Name) = LOWER(?) AND Password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n✅ Login Successful! Welcome, " + username + "!");
                return true;
            } else {
                System.out.println("\n❌ Invalid username or password. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        return false;
    }



    // Sign Up Method - Adds New User to User Table
    public void signUp() {
        System.out.println("\n===== 📝 Sign Up =====");

        // Generate a unique 5-digit User ID
        int userId = generateUniqueUserID();

        // Input Name with Format Validation
        String name;
        while (true) {
            System.out.print("Enter your Name (format: aaaa.aaaaa): ");
            name = scanner.nextLine().trim();

            if (isValidUsernameFormat(name)) {
                break; // Valid username format
            } else {
                System.out.println("❌ Invalid username format. Please use lowercase letters only, in the format: aaaa.aaaaa");
            }
        }

        // Input Password with Validation
        String password;
        while (true) {
            System.out.print("Enter Password (8+ characters, must include letters and numbers): ");
            password = scanner.nextLine().trim();

            if (isValidPassword(password)) {
                break;
            } else {
                System.out.println("❌ Invalid password. Ensure it is 8+ characters long and includes both letters and numbers.");
            }
        }

        // Insert the user into the database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO User (UserID, Name, Password) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.setString(3, password);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("\n✅ Sign Up Successful! Your User ID is: " + userId);
            } else {
                System.out.println("❌ Sign Up Failed. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: Unable to sign up. " + e.getMessage());
        }
    }

    // Generate Unique 5-Digit User ID
    private int generateUniqueUserID() {
        Random random = new Random();
        return 10000 + random.nextInt(90000); // Generate a 5-digit number
    }

    // Validate Username Format: aaaa.aaaaa
    private boolean isValidUsernameFormat(String name) {
        // Regex: lowercase letters only, with exactly one period
        return name.matches("^[a-z]+\\.[a-z]+$");
    }


    // Validate Password: At least 8 characters, includes letters and numbers
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }
}
