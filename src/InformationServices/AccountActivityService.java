package InformationServices;

import Connection.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AccountActivityService {
    private final Scanner scanner;

    public AccountActivityService(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showAccountActivitiesMenu() {
        int choice = -1;

        do {
            System.out.println("\n===== Account Activity Menu =====");
            System.out.println("1. Show All Account Activities");
            System.out.println("2. Show Activities by Account Number");
            System.out.println("3. Exit to Main Menu");
            System.out.print("Enter your choice: ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline
            } else {
                System.out.println("Invalid input. Please enter a number between 1 and 3.");
                scanner.nextLine(); // Clear invalid input
                continue;
            }

            switch (choice) {
                case 1 -> showAllAccountActivities();
                case 2 -> showActivitiesByAccountNumber();
                case 3 -> System.out.println("Returning to Main Menu...");
                default -> System.out.println("Invalid choice. Please enter a number between 1 and 3.");
            }
        } while (choice != 3);
    }

    // Show All Account Activities
    private void showAllAccountActivities() {
        int totalTransactions = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM accounthistory ORDER BY TransactionDate DESC";
            String countQuery = "SELECT COUNT(*) AS Total FROM accounthistory";

            // Execute count query for total transactions
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            ResultSet countResult = countStmt.executeQuery();
            if (countResult.next()) {
                totalTransactions = countResult.getInt("Total");
            }

            // Execute main query to fetch transaction data
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nAll Account Activities:");
            printHeader();

            if (!printResults(rs)) {
                System.out.println("No account activities found.");
            } else {
                System.out.println("---------------------------------------------------------------------------------------------------------------");
                System.out.printf("Total Transactions: %d\n", totalTransactions);
            }

        } catch (SQLException e) {
            System.err.println("SQL Error: Unable to retrieve account activities. " + e.getMessage());
        }
    }

    // Show Activities By Account Number
    private void showActivitiesByAccountNumber() {
        System.out.print("Enter Account Number to view activities: ");
        String accountNumber = scanner.nextLine().trim();

        if (accountNumber.isEmpty()) {
            System.out.println("Account number cannot be empty. Please try again.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM accounthistory WHERE SourceAccount=? OR DestinationAccount=? ORDER BY TransactionDate DESC";
            String countQuery = "SELECT COUNT(*) AS Total FROM accounthistory WHERE SourceAccount=? OR DestinationAccount=?";

            // Execute count query for total transactions
            int totalTransactions = 0;
            PreparedStatement countStmt = conn.prepareStatement(countQuery);
            countStmt.setString(1, accountNumber);
            countStmt.setString(2, accountNumber);
            ResultSet countResult = countStmt.executeQuery();
            if (countResult.next()) {
                totalTransactions = countResult.getInt("Total");
            }

            // Execute main query to fetch transaction data
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            stmt.setString(2, accountNumber);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nAccount Activity for Account Number: " + accountNumber);
            printHeader();

            if (!printResults(rs)) {
                System.out.println("No activity found for this account.");
            } else {
                System.out.println("---------------------------------------------------------------------------------------------------------------");
                System.out.printf("Total Transactions: %d\n", totalTransactions);
            }

        } catch (SQLException e) {
            System.err.println("SQL Error: Unable to retrieve account activity. " + e.getMessage());
        }
    }

    // Print Table Header
    private void printHeader() {
        System.out.printf("%-15s %-20s %-20s %-20s %-20s %-20s %-10s %-20s\n",
                "TransactionID", "Source Account", "Source Name", "Destination Account",
                "Destination Name", "Transaction Type", "Amount", "Transaction Date");
        System.out.println("---------------------------------------------------------------------------------------------------------------");
    }

    // Print Results and Return if Data Exists
    private boolean printResults(ResultSet rs) throws SQLException {
        boolean hasData = false;

        while (rs.next()) {
            hasData = true;
            System.out.printf("%-15d %-20s %-20s %-20s %-20s %-20s %-10.2f %-20s\n",
                    rs.getInt("TransactionID"),
                    rs.getString("SourceAccount"),
                    rs.getString("SourceName"),
                    rs.getString("DestinationAccount"),
                    rs.getString("DestinationName"),
                    rs.getString("TransactionType"),
                    rs.getDouble("Amount"),
                    rs.getTimestamp("TransactionDate"));
        }
        return hasData;
    }
}
