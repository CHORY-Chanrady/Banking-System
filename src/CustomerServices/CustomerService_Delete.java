package CustomerServices;

import Connection.DatabaseConnection;
import InformationServices.ShowCustomerService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class CustomerService_Delete {
    private Scanner scanner;

    public CustomerService_Delete(Scanner scanner) {
        this.scanner = scanner;
    }

    public void deleteCustomer() {
        if (scanner.hasNextLine()) {
            scanner.nextLine(); // Consume any leftover newline character
        }
        ShowCustomerService showcustomerservice = new ShowCustomerService(scanner);

        while (true) {
            showcustomerservice.showAllCustomers();
            System.out.println("\n=====Type cancel in any input to exit=====");
            int customerID = -1;
            while (true) {
                System.out.print("Enter Customer ID to delete: ");
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("cancel")) {
                    System.out.println("Deletion process canceled.");
                    return; // Exit the method if the user types "cancel"
                }

                try {
                    customerID = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid integer for Customer ID.");
                    continue; // Retry if the input is not a valid integer
                }

                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Check if the customer exists in the database
                    String checkCustomerQuery = "SELECT COUNT(*) FROM Customer WHERE CustomerID = ?";
                    try (PreparedStatement pstmtCheck = conn.prepareStatement(checkCustomerQuery)) {
                        pstmtCheck.setInt(1, customerID);
                        ResultSet rs = pstmtCheck.executeQuery();
                        if (rs.next() && rs.getInt(1) > 0) {
                            break; // Exit loop if the customer exists
                        } else {
                            System.out.println("Customer ID not found. Please try again or type 'cancel' to exit.");
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("SQL Error: " + e.getMessage());
                }
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Step 1: Check if there are associated accounts
                String countAccountsQuery = "SELECT COUNT(*) FROM Account WHERE CustomerID = ?";
                try (PreparedStatement pstmtCount = conn.prepareStatement(countAccountsQuery)) {
                    pstmtCount.setInt(1, customerID);
                    ResultSet rs = pstmtCount.executeQuery();
                    if (rs.next()) {
                        int associatedAccounts = rs.getInt(1);
                        if (associatedAccounts > 0) {
                            System.out.println("This customer has " + associatedAccounts + " associated account(s).");
                            System.out.print("Are you sure you want to delete these associated accounts? (yes/no): ");
                            String confirmation = scanner.nextLine().trim().toLowerCase();
                            if (!confirmation.equals("yes")) {
                                System.out.println("Deletion canceled.");
                                continue; // Restart the process for a new customer ID
                            }
                        } else {
                            System.out.println("No associated accounts found for this customer.");
                        }
                    }
                }

                // Step 2: Temporarily disable foreign key checks
                conn.createStatement().execute("SET foreign_key_checks = 0;");

                // Step 3: Move associated accounts to the DeletedAccount table
                String insertAccountsQuery = "INSERT INTO DeletedAccount (AccountNumber, AccountName, AccountType, Currency, CustomerID, CreationDate, Balance) " +
                        "SELECT AccountNumber, AccountName, AccountType, Currency, CustomerID, CreationDate, Balance " +
                        "FROM Account WHERE CustomerID = ?";
                try (PreparedStatement pstmtInsertAccounts = conn.prepareStatement(insertAccountsQuery)) {
                    pstmtInsertAccounts.setInt(1, customerID);
                    int rowsInserted = pstmtInsertAccounts.executeUpdate();
                    if (rowsInserted < 0) {
                        System.out.println("No associated accounts.");
                    }
                }

                // Step 4: Delete all associated accounts from the Account table
                String deleteAccountsQuery = "DELETE FROM Account WHERE CustomerID = ?";
                try (PreparedStatement pstmtDeleteAccounts = conn.prepareStatement(deleteAccountsQuery)) {
                    pstmtDeleteAccounts.setInt(1, customerID);
                    int rowsDeleted = pstmtDeleteAccounts.executeUpdate();
                    if (rowsDeleted > 0) {
                        System.out.println("Associated accounts deleted successfully.");
                    }
                }

                // Step 5: Move customer data to the DeletedCustomer table
                String insertQuery = "INSERT INTO DeletedCustomer (CustomerID, CustomerName, CustomerSex, DateOfBirth, Nationality, PlaceOfBirth, Email, CurrentAddress, CreationDate) " +
                        "SELECT CustomerID, CustomerName, CustomerSex, DateOfBirth, Nationality, PlaceOfBirth, Email, CurrentAddress, CreationDate " +
                        "FROM Customer WHERE CustomerID = ?";
                try (PreparedStatement pstmtInsert = conn.prepareStatement(insertQuery)) {
                    pstmtInsert.setInt(1, customerID);
                    int rowsInserted = pstmtInsert.executeUpdate();
                    if (rowsInserted < 0) {
                        System.out.println("Customer not found.");
                    }
                }

                // Step 6: Delete the customer from the Customer table
                String deleteQuery = "DELETE FROM Customer WHERE CustomerID = ?";
                try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteQuery)) {
                    pstmtDelete.setInt(1, customerID);
                    int rowsDeleted = pstmtDelete.executeUpdate();
                    if (rowsDeleted > 0) {
                        System.out.println("Customer deleted successfully!");
                    } else {
                        System.out.println("Failed to delete the customer. It may not exist.");
                    }
                }

                // Step 7: Re-enable foreign key checks
                conn.createStatement().execute("SET foreign_key_checks = 1;");

            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.createStatement().execute("SET foreign_key_checks = 1;"); // Ensure checks are re-enabled on error
                } catch (SQLException ex) {
                    System.err.println("Error re-enabling foreign key checks: " + ex.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
