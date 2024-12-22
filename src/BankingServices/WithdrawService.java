package BankingServices;

import Connection.DatabaseConnection;
import InformationServices.ShowAccountService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;

public class WithdrawService {
    private final Scanner scanner;

    public WithdrawService(Scanner scanner) {
        this.scanner = scanner;
    }

    public void withdraw() {
        if (scanner.hasNextLine()) {
            scanner.nextLine(); // Consume any leftover newline character
        }

        ShowAccountService showAllAccounts = new ShowAccountService(scanner);
        showAllAccounts.showAllAccounts();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String selectedAccount;
            String accountName = null;
            String accountCurrency = null;
            double currentBalance = 0.0;

            // Step 1: Validate Account
            while (true) {
                System.out.print("Enter Account Number (enter '0' to exit): ");
                selectedAccount = scanner.nextLine().trim();

                if (selectedAccount.equals("0")) {
                    System.out.println("Withdrawal process canceled.");
                    return;
                }

                String checkAccountQuery = "SELECT AccountName, Currency, Balance FROM Account WHERE AccountNumber = ?";
                try (PreparedStatement stmt = conn.prepareStatement(checkAccountQuery)) {
                    stmt.setString(1, selectedAccount);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        accountName = rs.getString("AccountName");
                        accountCurrency = rs.getString("Currency");
                        currentBalance = rs.getDouble("Balance");
                        break;
                    } else {
                        System.out.println("❌ Account number not found. Please try again.");
                    }
                }
            }

            // Step 2: Select Currency
            String withdrawCurrency = selectCurrency();

            // Step 3: Enter Withdrawal Amount
            double withdrawAmount;
            while (true) {
                System.out.print("Enter Amount to Withdraw (enter '0' to exit): ");
                String input = scanner.nextLine().trim();

                if (input.equals("0")) {
                    System.out.println("Withdrawal process canceled.");
                    return;
                }

                try {
                    withdrawAmount = Double.parseDouble(input);

                    double convertedAmount = convertCurrency(withdrawAmount, withdrawCurrency, accountCurrency);

                    if (convertedAmount > 0 && convertedAmount <= currentBalance) {
                        performWithdrawal(conn, selectedAccount, accountName, accountCurrency, withdrawCurrency, withdrawAmount, convertedAmount);
                        return; // Successful withdrawal, exit method
                    } else if (convertedAmount > currentBalance) {
                        System.out.println("❌ Insufficient balance. Current balance: " + currentBalance + " " + accountCurrency);
                    } else {
                        System.out.println("❌ Amount must be greater than zero.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Invalid input. Please enter a valid numeric amount.");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    private String selectCurrency() {
        String currency;
        while (true) {
            System.out.print("Select Currency (USD / KHR): ");
            currency = scanner.nextLine().trim().toUpperCase();
            if (currency.equals("USD") || currency.equals("KHR")) {
                return currency;
            } else {
                System.out.println("❌ Invalid currency. Please enter USD or KHR.");
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
        }
        return amount; // Default to no conversion
    }

    private void performWithdrawal(Connection conn, String accountNumber, String accountName,
                                   String accountCurrency, String withdrawCurrency,
                                   double withdrawAmount, double convertedAmount) throws SQLException {
        String updateQuery = "UPDATE Account SET Balance = Balance - ? WHERE AccountNumber = ?";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
            updateStmt.setDouble(1, convertedAmount);
            updateStmt.setString(2, accountNumber);

            int rowsUpdated = updateStmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Withdrawal Successful: " + withdrawAmount + " " + withdrawCurrency +
                        " withdrawn from account " + accountNumber);

                // Log the transaction
                insertTransactionHistory(conn, accountNumber, accountName, withdrawCurrency, withdrawAmount);
            } else {
                System.out.println("❌ Withdrawal failed. Please try again.");
            }
        }
    }

    private void insertTransactionHistory(Connection conn, String accountNumber, String accountName,
                                          String currency, double amount) throws SQLException {
        String insertQuery = "INSERT INTO accounthistory (SourceAccount, SourceName, TransactionType, Currency, Amount, TransactionDate) " +
                "VALUES (?, ?, 'Withdraw', ?, ?, ?)";

        try (PreparedStatement historyStmt = conn.prepareStatement(insertQuery)) {
            historyStmt.setString(1, accountNumber);
            historyStmt.setString(2, accountName);
            historyStmt.setString(3, currency);
            historyStmt.setDouble(4, amount);
            historyStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            historyStmt.executeUpdate();
        }
    }
}
