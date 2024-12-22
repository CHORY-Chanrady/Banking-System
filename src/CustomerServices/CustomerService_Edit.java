package CustomerServices;

import Connection.DatabaseConnection;
import InformationServices.ShowCustomerService;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class CustomerService_Edit {
    private Scanner scanner;

    public CustomerService_Edit(Scanner scanner) {
        this.scanner = scanner;
    }

    public void editCustomer() {
        ShowCustomerService showcustomerservice = new ShowCustomerService(scanner);
        showcustomerservice.showAllCustomers();

        int customerId = -1;
        boolean validId = false;

        // Loop until a valid customer ID or 0 is entered
        System.out.println("\n=====Enter number '0' to exit=====");
        while (!validId) {
            System.out.print("Enter Customer ID: ");
            if (scanner.hasNextInt()) {
                customerId = scanner.nextInt();
                scanner.nextLine(); // Consume newline after reading int

                if (customerId == 0) {
                    System.out.println("Exiting edit mode.");
                    return;
                }

                try (Connection conn = DatabaseConnection.getConnection()) {
                    String selectSQL = "SELECT * FROM Customer WHERE CustomerID = ?";
                    PreparedStatement stmt = conn.prepareStatement(selectSQL);
                    stmt.setInt(1, customerId);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        // Customer found, proceed with editing
                        validId = true;

                        // Get the number of accounts associated with the customer
                        int accountCount = getAccountCount(conn, customerId);

                        // Display current customer details
                        System.out.println("Customer found. Current details:");
                        System.out.println("1. Name: " + rs.getString("CustomerName"));
                        System.out.println("2. Sex: " + rs.getString("CustomerSex"));
                        System.out.println("3. Date of Birth: " + rs.getDate("DateOfBirth"));
                        System.out.println("4. Nationality: " + rs.getString("Nationality"));
                        System.out.println("5. Place of Birth: " + rs.getString("PlaceOfBirth"));
                        System.out.println("6. Email: " + rs.getString("Email"));
                        System.out.println("7. Current Address: " + rs.getString("CurrentAddress"));
                        System.out.println("8. Save and Exit");

                        boolean continueEditing = true;
                        while (continueEditing) {
                            System.out.println("\n===== Type 'cancel' in any input to exit the operation =====");
                            System.out.print("Which detail would you like to edit? (1-8): ");
                            int choice = scanner.nextInt();
                            scanner.nextLine(); // Consume newline after reading int

                            switch (choice) {
                                case 1:
                                    System.out.print("Enter new Name: ");
                                    String newName = scanner.nextLine().trim();
                                    if (isCancelInput(newName)) break;
                                    if (newName.isEmpty()) {
                                        System.out.println("Name cannot be blank.");
                                        break;
                                    }
                                    // Update customer name
                                    updateCustomerDetail(conn, customerId, "CustomerName", newName);

                                    // Also update all associated accounts
                                    updateAccountsWithNewCustomerName(conn, customerId, newName);
                                    break;
                                case 2:
                                    System.out.print("Enter new Sex (M/F): ");
                                    String newSex = scanner.nextLine().trim().toUpperCase();
                                    if (isCancelInput(newSex)) break;
                                    if (newSex.equals("M") || newSex.equals("F")) {
                                        updateCustomerDetail(conn, customerId, "CustomerSex", newSex);
                                    } else {
                                        System.out.println("Invalid input. Please enter 'M' or 'F'.");
                                    }
                                    break;
                                case 3:
                                    System.out.print("Enter new Date of Birth (YYYY-MM-DD): ");
                                    String dobInput = scanner.nextLine().trim();
                                    if (isCancelInput(dobInput)) break;
                                    try {
                                        LocalDate newDob = LocalDate.parse(dobInput, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                        updateCustomerDetail(conn, customerId, "DateOfBirth", newDob);
                                    } catch (Exception e) {
                                        System.out.println("Invalid date format. Please enter in YYYY-MM-DD format.");
                                    }
                                    break;
                                case 4:
                                    System.out.print("Enter new Nationality: ");
                                    String newNationality = scanner.nextLine().trim();
                                    if (isCancelInput(newNationality)) break;
                                    if (newNationality.isEmpty()) {
                                        System.out.println("Nationality cannot be blank.");
                                        break;
                                    }
                                    updateCustomerDetail(conn, customerId, "Nationality", newNationality);
                                    break;
                                case 5:
                                    System.out.print("Enter new Place of Birth: ");
                                    String newPlaceOfBirth = scanner.nextLine().trim();
                                    if (isCancelInput(newPlaceOfBirth)) break;
                                    if (newPlaceOfBirth.isEmpty()) {
                                        System.out.println("Place of Birth cannot be blank.");
                                        break;
                                    }
                                    updateCustomerDetail(conn, customerId, "PlaceOfBirth", newPlaceOfBirth);
                                    break;
                                case 6:
                                    System.out.print("Enter new Email: ");
                                    String newEmail = scanner.nextLine().trim();
                                    if (isCancelInput(newEmail)) break;
                                    if (newEmail.isEmpty()) {
                                        System.out.println("Email cannot be blank.");
                                        break;
                                    }
                                    updateCustomerDetail(conn, customerId, "Email", newEmail);
                                    break;
                                case 7:
                                    System.out.print("Enter new Address: ");
                                    String newAddress = scanner.nextLine().trim();
                                    if (isCancelInput(newAddress)) break;
                                    if (newAddress.isEmpty()) {
                                        System.out.println("Address cannot be blank.");
                                        break;
                                    }
                                    updateCustomerDetail(conn, customerId, "CurrentAddress", newAddress);
                                    break;
                                case 8:
                                    System.out.println("Changes saved. Exiting edit mode.");
                                    continueEditing = false;
                                    break;
                                default:
                                    System.out.println("Invalid choice. Please enter a number between 1 and 9.");
                                    break;
                            }
                        }
                    } else {
                        System.out.println("Customer not found. Please try again.");
                    }
                } catch (SQLException e) {
                    System.err.println("SQL Error: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid input. Please enter a valid integer.");
                scanner.nextLine(); // Consume invalid input
            }
        }
    }

    private boolean isCancelInput(String input) {
        return input.equalsIgnoreCase("cancel");
    }

    private void updateCustomerDetail(Connection conn, int customerId, String columnName, Object newValue) throws SQLException {
        String updateSQL = "UPDATE Customer SET " + columnName + " = ? WHERE CustomerID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
            if (newValue instanceof String) {
                stmt.setString(1, (String) newValue);
            } else if (newValue instanceof LocalDate) {
                stmt.setDate(1, Date.valueOf((LocalDate) newValue));
            }
            stmt.setInt(2, customerId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println(columnName + " updated successfully!");
            } else {
                System.out.println("Failed to update " + columnName + ".");
            }
        }
    }

    private void updateAccountsWithNewCustomerName(Connection conn, int customerId, String newName) throws SQLException {
        String updateSQL = "UPDATE Account SET AccountName = ? WHERE CustomerID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
            stmt.setString(1, newName);
            stmt.setInt(2, customerId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("All associated accounts updated with new customer name.");
            } else {
                System.out.println("Failed to update associated accounts.");
            }
        }
    }

    private int getAccountCount(Connection conn, int customerId) throws SQLException {
        String countSQL = "SELECT COUNT(*) FROM Account WHERE CustomerID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(countSQL)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // Return the count of accounts
            }
        }
        return 0;
    }
}
