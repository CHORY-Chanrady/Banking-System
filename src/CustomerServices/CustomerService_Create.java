package CustomerServices;

import Connection.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CustomerService_Create {
    private Scanner scanner;

    public CustomerService_Create(Scanner scanner) {
        this.scanner = scanner;
    }

    public void createCustomer() {
        if (scanner.hasNextLine()) {
            scanner.nextLine(); // Consume any leftover newline character
        }
        System.out.println("\n===== Type cancel in any input to exit=====");
        // Get the customer name
        String name = getInput("Enter Customer Name: ", "Customer name cannot be empty.");
        if (name.equalsIgnoreCase("cancel")) return;

        // Get the customer sex (M/F)
        char sex = getCharInput("Enter Customer Sex (M/F): ", "Invalid input. Please enter 'M' or 'F'.", 'M', 'F');
        if (sex == '\0') return;

        // Get the date of birth (DD-MM-YYYY)
        LocalDate dob = getDateInput("Enter Date of Birth (DD-MM-YYYY):", "Invalid date format. Please enter in DD-MM-YYYY format.");
        if (dob == null) return;

        // Get the nationality
        String nationality = getInput("Enter Nationality: ", "Nationality cannot be empty.");
        if (nationality.equalsIgnoreCase("cancel")) return;

        // Get the place of birth
        String placeOfBirth = getInput("Enter Place of Birth: ", "Place of birth cannot be empty.");
        if (placeOfBirth.equalsIgnoreCase("cancel")) return;

        // Get the email
        String email = getEmailInput("Enter Email: ", "Invalid email format. Please enter a valid email.");
        if (email.equalsIgnoreCase("cancel")) return;

        // Get the current address
        String address = getInput("Enter Current Address:", "Address cannot be empty.");
        if (address.equalsIgnoreCase("cancel")) return;

        // Insert the data into the database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Customer (CustomerName, CustomerSex, DateOfBirth, Nationality, PlaceOfBirth, Email, CurrentAddress) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, String.valueOf(sex));
            stmt.setDate(3, Date.valueOf(dob));
            stmt.setString(4, nationality);
            stmt.setString(5, placeOfBirth);
            stmt.setString(6, email);
            stmt.setString(7, address);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Customer created successfully!");
            } else {
                System.out.println("Failed to create customer.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private String getInput(String prompt, String errorMessage) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel") || !input.isEmpty()) {
                return input;
            }
            System.out.println(errorMessage);
        }
    }

    private char getCharInput(String prompt, String errorMessage, char... validOptions) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equalsIgnoreCase("cancel")) return '\0';
            if (input.length() == 1) {
                char charInput = input.charAt(0);
                for (char option : validOptions) {
                    if (charInput == option) {
                        return charInput;
                    }
                }
            }
            System.out.println(errorMessage);
        }
    }

    private LocalDate getDateInput(String prompt, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) return null;
            try {
                return LocalDate.parse(input, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (Exception e) {
                System.out.println(errorMessage);
            }
        }
    }

    private String getEmailInput(String prompt, String errorMessage) {
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        while (true) {
            System.out.print(prompt);
            String email = scanner.nextLine().trim();
            if (email.equalsIgnoreCase("cancel")) return "cancel";
            if (emailPattern.matcher(email).matches()) {
                return email;
            }
            System.out.println(errorMessage);
        }
    }
}
