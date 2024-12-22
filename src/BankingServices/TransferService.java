package BankingServices;

import Connection.DatabaseConnection;
import InformationServices.ShowAccountService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;

public class TransferService {
    private final Scanner scanner;

    public TransferService(Scanner scanner) {
        this.scanner = scanner;
    }

    public void transferMoney() {
        ShowAccountService showAllAccounts = new ShowAccountService(scanner);

        // Step 1: Display all accounts
        showAllAccounts.showAllAccounts();
        System.out.println("\n===== \ud83d\udcb8 Transfer Money =====");

        System.out.println("Enter '0' at any time to cancel the transfer.");
        // Step 2: Collect account details
        System.out.print("Enter Source Account Number: ");
        String sourceAccount = scanner.nextLine().trim();
        if (sourceAccount.equals("0")) {
            System.out.println("\u274c Transfer canceled.");
            return;
        }

        System.out.print("Enter Destination Account Number: ");
        String destinationAccount = scanner.nextLine().trim();
        if (destinationAccount.equals("0")) {
            System.out.println("\u274c Transfer canceled.");
            return;
        }

        if (sourceAccount.isEmpty() || destinationAccount.isEmpty()) {
            System.out.println("\u274c Account numbers cannot be empty. Please try again.");
            return;
        }

        if (sourceAccount.equals(destinationAccount)) {
            System.out.println("\u274c Source and destination accounts cannot be the same.");
            return;
        }

        System.out.print("Select Currency (USD/KHR): ");
        String chosenCurrency = scanner.nextLine().trim().toUpperCase();
        if (chosenCurrency.equals("0")) {
            System.out.println("\u274c Transfer canceled.");
            return;
        }

        if (!chosenCurrency.equals("USD") && !chosenCurrency.equals("KHR")) {
            System.out.println("\u274c Invalid currency. Only USD or KHR is allowed.");
            return;
        }

        System.out.print("Enter Amount: ");
        double transferAmount;

        if (scanner.hasNextDouble()) {
            transferAmount = scanner.nextDouble();
            scanner.nextLine(); // Consume newline
        } else {
            System.out.println("\u274c Invalid amount. Please enter a numeric value.");
            scanner.nextLine();
            return;
        }

        if (transferAmount == 0) {
            System.out.println("\u274c Transfer canceled.");
            return;
        }

        if (transferAmount <= 0) {
            System.out.println("\u274c Transfer amount must be greater than zero.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Step 3: Validate accounts
            String sourceName = validateAccount(conn, sourceAccount, transferAmount, chosenCurrency, true);
            String destinationName = validateAccount(conn, destinationAccount, transferAmount, chosenCurrency, false);

            if (sourceName == null || destinationName == null) {
                conn.rollback();
                return;
            }

            String sourceCurrency = getAccountCurrency(conn, sourceAccount);
            String destinationCurrency = getAccountCurrency(conn, destinationAccount);

            double sourceDeductionAmount = transferAmount;
            double destinationDepositAmount = transferAmount;

            // Step 4: Handle cross-currency transfers
            if (!chosenCurrency.equals(sourceCurrency)) {
                sourceDeductionAmount = convertCurrency(transferAmount, chosenCurrency, sourceCurrency);
            }

            if (!chosenCurrency.equals(destinationCurrency)) {
                destinationDepositAmount = convertCurrency(transferAmount, chosenCurrency, destinationCurrency);
            }

            // Update balances
            updateAccountBalance(conn, sourceAccount, -sourceDeductionAmount);
            updateAccountBalance(conn, destinationAccount, destinationDepositAmount);

            // Log the transaction
            insertTransaction(conn, sourceAccount, sourceName, destinationAccount, destinationName,
                    "Transfer", chosenCurrency, transferAmount);

            conn.commit(); // Commit transaction
            System.out.printf("\u2705 Transfer Successful! Transferred %.2f %s from %s (%s) to %s (%s).\n",
                    transferAmount, chosenCurrency, sourceAccount, sourceCurrency, destinationAccount, destinationCurrency);

        } catch (SQLException e) {
            System.err.println("SQL Error during transfer: " + e.getMessage());
            rollbackTransaction();
        }
    }

    private String validateAccount(Connection conn, String accountNumber, double requiredBalance, String chosenCurrency, boolean isSource) throws SQLException {
        String query = "SELECT AccountName, Balance, Currency FROM account WHERE AccountNumber = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                System.out.println("\u274c " + (isSource ? "Source" : "Destination") + " account does not exist.");
                return null;
            }

            String accountCurrency = rs.getString("Currency");
            double balance = rs.getDouble("Balance");
            String accountName = rs.getString("AccountName");

            // Convert required balance to the account's currency if needed
            double requiredBalanceInAccountCurrency = requiredBalance;
            if (!accountCurrency.equals(chosenCurrency)) {
                requiredBalanceInAccountCurrency = convertCurrency(requiredBalance, chosenCurrency, accountCurrency);
            }

            if (isSource && balance < requiredBalanceInAccountCurrency) {
                System.out.printf("\u274c Insufficient balance in the source account. Required: %.2f %s, Available: %.2f %s\n",
                        requiredBalanceInAccountCurrency, accountCurrency, balance, accountCurrency);
                return null;
            }

            return accountName;
        }
    }

    private String getAccountCurrency(Connection conn, String accountNumber) throws SQLException {
        String query = "SELECT Currency FROM account WHERE AccountNumber = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Currency");
            } else {
                throw new SQLException("Currency not found for account: " + accountNumber);
            }
        }
    }

    private double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount; // No conversion needed
        } else if (fromCurrency.equals("USD") && toCurrency.equals("KHR")) {
            return amount * 4000; // USD to KHR
        } else if (fromCurrency.equals("KHR") && toCurrency.equals("USD")) {
            return amount / 4000; // KHR to USD
        } else {
            throw new IllegalArgumentException("Unsupported currency conversion: " + fromCurrency + " to " + toCurrency);
        }
    }

    private void updateAccountBalance(Connection conn, String accountNumber, double amount) throws SQLException {
        String updateQuery = "UPDATE account SET Balance = Balance + ? WHERE AccountNumber = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, accountNumber);
            stmt.executeUpdate();
        }
    }

    private void insertTransaction(Connection conn, String sourceAccount, String sourceName,
                                   String destinationAccount, String destinationName,
                                   String transactionType, String currency, double amount) throws SQLException {
        String insertQuery = "INSERT INTO accounthistory (SourceAccount, SourceName, DestinationAccount, DestinationName, " +
                "TransactionType, Currency, Amount, TransactionDate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, sourceAccount);
            stmt.setString(2, sourceName);
            stmt.setString(3, destinationAccount);
            stmt.setString(4, destinationName);
            stmt.setString(5, transactionType);
            stmt.setString(6, currency);
            stmt.setDouble(7, amount);
            stmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        }
    }

    private void rollbackTransaction() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                conn.rollback();
                System.out.println("Transaction rolled back.");
            }
        } catch (SQLException e) {
            System.err.println("Rollback failed: " + e.getMessage());
        }
    }
}
