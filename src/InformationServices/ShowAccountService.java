package InformationServices;

import Connection.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ShowAccountService {
    private final Scanner scanner;

    public ShowAccountService(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showAccountsMenu() {
        int choice = -1;

        do {
            try {
                System.out.println("\n===== Show Account Menu =====");
                System.out.println("1. Show All Accounts");
                System.out.println("2. Show Account by Account Number");
                System.out.println("3. Back To Main Menu");
                System.out.print("Choose an option: ");

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline character
                } else {
                    System.out.println("Invalid input! Please enter a valid number.");
                    scanner.next(); // Clear invalid input
                    continue;
                }

                switch (choice) {
                    case 1 -> showAllAccounts();
                    case 2 -> showAccountInformation();
                    case 3 -> System.out.println("Returning to Main Menu...");
                    default -> System.out.println("Invalid choice. Please enter a number between 1 and 3.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a valid number.");
                scanner.next(); // Clear invalid input
            }
        } while (choice != 3);
    }

    public void showAllAccounts() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to count total accounts
            String countQuery = "SELECT COUNT(*) AS TotalAccounts FROM Account";
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            ResultSet countRs = countStmt.executeQuery();
            int totalAccounts = 0;

            if (countRs.next()) {
                totalAccounts = countRs.getInt("TotalAccounts");
            }

            // Display the total in the header
            System.out.println("\nAll Accounts: (Total: " + totalAccounts + ")");
            System.out.printf("%-5s %-15s %-20s %-10s %-10s %-10s %-15s %-15s %-10s\n",
                    "No.", "Account Number", "Account Name", "Currency", "Balance", "Account Type", "Interest Rate", "Creation Date", "Customer ID");
            System.out.println("---------------------------------------------------------------------------------------------------");

            // Query to fetch account details
            String query = "SELECT AccountNumber, AccountName, Currency, Balance, AccountType, InterestRate, CreationDate, CustomerID FROM Account";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            int count = 1;
            while (rs.next()) {
                System.out.printf("%-5d %-15s %-20s %-10s %-10.2f %-15s %-13.2f %-18s %-10d\n",
                        count++,
                        rs.getString("AccountNumber"),
                        rs.getString("AccountName"),
                        rs.getString("Currency"),
                        rs.getDouble("Balance"),
                        rs.getString("AccountType"),
                        rs.getDouble("InterestRate"),
                        rs.getDate("CreationDate"),
                        rs.getInt("CustomerID"));
            }

            if (count == 1) { // If no rows were printed
                System.out.println("No accounts found.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: Unable to retrieve account data. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: An unexpected error occurred. " + e.getMessage());
        }
    }

    private void showAccountInformation() {
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine().trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Account WHERE AccountNumber = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("\nAccount Information:");
                System.out.println("---------------------------------------------------");
                System.out.printf("%-20s: %s\n", "Account Number", rs.getString("AccountNumber"));
                System.out.printf("%-20s: %s\n", "Account Name", rs.getString("AccountName"));
                System.out.printf("%-20s: %s\n", "Account Type", rs.getString("AccountType"));
                System.out.printf("%-20s: %s\n", "Currency", rs.getString("Currency"));
                System.out.printf("%-20s: %.2f%%\n", "Interest Rate", rs.getDouble("InterestRate"));
                System.out.printf("%-20s: %.2f\n", "Balance", rs.getDouble("Balance"));
                System.out.printf("%-20s: %s\n", "Creation Date", rs.getDate("CreationDate"));
                System.out.printf("%-20s: %d\n", "Customer ID", rs.getInt("CustomerID"));
                System.out.println("---------------------------------------------------");
            } else {
                System.out.println("Account not found. Please check the account number and try again.");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: Unable to retrieve account information. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: An unexpected error occurred. " + e.getMessage());
        }
    }
}
