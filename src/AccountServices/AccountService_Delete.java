package AccountServices;

import Connection.DatabaseConnection;
import InformationServices.ShowAccountService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class AccountService_Delete {
    private Scanner scanner;

    public AccountService_Delete(Scanner scanner) {
        this.scanner = scanner;
    }

    public void deleteAccount() {
        if (scanner.hasNextLine()) {
            scanner.nextLine(); // Consume any leftover newline character
        }
        ShowAccountService showallaccounts = new ShowAccountService(scanner);
        // Display all accounts
        showallaccounts.showAllAccounts();
        System.out.print("Enter Account Number to delete: ");
        String accountNumber = scanner.nextLine().trim(); // Using nextLine() to read the whole line

        // Validate input
        if (accountNumber.isEmpty()) {
            System.out.println("Account number cannot be empty.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Move data to the DeletedAccount table
            String insertQuery = "INSERT INTO DeletedAccount (AccountNumber, CustomerID, AccountName, AccountType, Currency, Balance, InterestRate, Period, CreationDate) " +
                    "SELECT AccountNumber, CustomerID, AccountName, AccountType, Currency, Balance, InterestRate, Period, CreationDate " +
                    "FROM Account WHERE AccountNumber = ?";
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertQuery)) {
                pstmtInsert.setString(1, accountNumber); // Set the AccountNumber
                int rowsInserted = pstmtInsert.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Data successfully moved to DeletedAccount table.");
                } else {
                    System.out.println("No data found for the given AccountNumber.");
                    conn.rollback(); // Rollback transaction if no data is found
                    return;
                }
            }

            // Step 2: Delete data from the original Account table
            String deleteQuery = "DELETE FROM Account WHERE AccountNumber = ?";
            try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteQuery)) {
                pstmtDelete.setString(1, accountNumber); // Set the AccountNumber
                int rowsDeleted = pstmtDelete.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("Account deleted successfully.");
                } else {
                    System.out.println("Failed to delete the account. It may not exist.");
                    conn.rollback(); // Rollback transaction if delete fails
                    return;
                }
            }

            // Commit the transaction if both operations succeed
            conn.commit();
            System.out.println("Transaction completed successfully.");

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction in case of an error
                    System.out.println("Transaction rolled back due to an error.");
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
