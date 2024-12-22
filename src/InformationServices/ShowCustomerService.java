package InformationServices;

import Connection.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ShowCustomerService {
    private final Scanner scanner;

    public ShowCustomerService(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showCustomerInformation() {
        int choice = -1;

        do {
            try {
                System.out.println("\n===== Customer Information Menu =====");
                System.out.println("1. Show All Customers");
                System.out.println("2. Show Customer by ID");
                System.out.println("3. Back to Main Menu");
                System.out.print("Enter your choice: ");

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                } else {
                    System.out.println("Invalid input. Please enter a number between 1 and 3.");
                    scanner.next(); // Clear invalid input
                    continue;
                }

                switch (choice) {
                    case 1 -> showAllCustomers();
                    case 2 -> showCustomerByID();
                    case 3 -> System.out.println("Returning to Main Menu...");
                    default -> System.out.println("Invalid choice. Please enter a number between 1 and 3.");
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                scanner.next(); // Clear invalid input
            }
        } while (choice != 3);
    }

    public void showAllCustomers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to count total customers
            String countQuery = "SELECT COUNT(*) AS TotalCustomers FROM Customer";
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            ResultSet countRs = countStmt.executeQuery();
            int totalCustomers = 0;

            if (countRs.next()) {
                totalCustomers = countRs.getInt("TotalCustomers");
            }

            // Display the total in the header
            System.out.println("\nAll Customers: (Total: " + totalCustomers + ")");
            System.out.printf("%-10s %-20s %-10s %-15s %-15s %-25s %-30s %-25s %-10s\n",
                    "CustomerID", "Name", "Sex", "Date of Birth", "Nationality", "Place of Birth", "Email", "Current Address", "Accounts");
            System.out.println("--------------------------------------------------------------------------------------------------------------");

            // Query to fetch customer details and associated account count
            String query = "SELECT c.*, COUNT(a.AccountID) AS AccountCount " +
                    "FROM Customer c LEFT JOIN Account a ON c.CustomerID = a.CustomerID " +
                    "GROUP BY c.CustomerID";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            int customerCount = 0;
            while (rs.next()) {
                customerCount++; // Increment the counter for each customer
                System.out.printf("%-10d %-20s %-10s %-15s %-15s %-25s %-30s %-25s %-10d\n",
                        rs.getInt("CustomerID"),
                        rs.getString("CustomerName"),
                        rs.getString("CustomerSex"),
                        rs.getDate("DateOfBirth"),
                        rs.getString("Nationality"),
                        rs.getString("PlaceOfBirth"),
                        rs.getString("Email"),
                        rs.getString("CurrentAddress"),
                        rs.getInt("AccountCount"));
            }

            if (customerCount == 0) {
                System.out.println("No customers found.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: Unable to retrieve customer data. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: An unexpected error occurred. " + e.getMessage());
        }
    }

    private void showCustomerByID() {
        int customerID = -1;

        while (true) {
            System.out.print("Enter Customer ID: ");
            try {
                customerID = Integer.parseInt(scanner.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric Customer ID.");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to fetch customer details and account count by ID
            String query = "SELECT c.*, COUNT(a.AccountID) AS AccountCount " +
                    "FROM Customer c LEFT JOIN Account a ON c.CustomerID = a.CustomerID " +
                    "WHERE c.CustomerID = ? " +
                    "GROUP BY c.CustomerID";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, customerID);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("\nCustomer Information:");
                System.out.println("----------------------------------------------------");
                System.out.printf("%-20s: %s\n", "Customer ID", rs.getInt("CustomerID"));
                System.out.printf("%-20s: %s\n", "Name", rs.getString("CustomerName"));
                System.out.printf("%-20s: %s\n", "Sex", rs.getString("CustomerSex"));
                System.out.printf("%-20s: %s\n", "Date of Birth", rs.getDate("DateOfBirth"));
                System.out.printf("%-20s: %s\n", "Nationality", rs.getString("Nationality"));
                System.out.printf("%-20s: %s\n", "Place of Birth", rs.getString("PlaceOfBirth"));
                System.out.printf("%-20s: %s\n", "Email", rs.getString("Email"));
                System.out.printf("%-20s: %s\n", "Current Address", rs.getString("CurrentAddress"));
                System.out.printf("%-20s: %d\n", "Associated Accounts", rs.getInt("AccountCount"));
                System.out.println("----------------------------------------------------");
            } else {
                System.out.println("Customer not found. Please check the Customer ID and try again.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: Unable to retrieve customer data. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: An unexpected error occurred. " + e.getMessage());
        }
    }


    public String getCustomerNameById(int customerId) {
        String customerName = null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT CustomerName FROM Customer WHERE CustomerID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, customerId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                customerName = rs.getString("CustomerName");
            } else {
                System.out.println("Customer not found for ID: " + customerId);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }

        return customerName;
    }
}
