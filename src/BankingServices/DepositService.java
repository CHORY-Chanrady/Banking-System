package BankingServices;

import Connection.DatabaseConnection;
import InformationServices.ShowAccountService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;

public class DepositService {
    private Scanner scanner;

    public DepositService(Scanner scanner) {
        this.scanner = scanner;
    }

    public void deposit() {
        if (scanner.hasNextLine()) {
            scanner.nextLine(); // Consume any leftover newline character
        }

        ShowAccountService showallaccounts = new ShowAccountService(scanner);
        showallaccounts.showAllAccounts();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String selectedAccount;
            String accountName = null;
            String accountCurrency = null;

            // Step 1: Validate Account
            while (true) {
                System.out.print("Enter Account Number (enter '0' to exit): ");
                selectedAccount = scanner.nextLine().trim();

                if (selectedAccount.equals("0")) {
                    System.out.println("Deposit process canceled.");
                    return;
                }

                String checkAccountQuery = "SELECT AccountName, Currency FROM Account WHERE AccountNumber = ?";
                try (PreparedStatement stmt = conn.prepareStatement(checkAccountQuery)) {
                    stmt.setString(1, selectedAccount);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        accountName = rs.getString("AccountName");
                        accountCurrency = rs.getString("Currency");
                        break;
                    } else {
                        System.out.println("❌ Account not found. Please try again.");
                    }
                }
            }

            // Step 2: Currency Selection
            String depositCurrency;
            while (true) {
                System.out.print("Select Currency (USD/KHR): ");
                depositCurrency = scanner.nextLine().trim().toUpperCase();
                if (depositCurrency.equals("USD") || depositCurrency.equals("KHR")) {
                    break;
                } else {
                    System.out.println("❌ Invalid currency. Enter USD or KHR.");
                }
            }

            // Step 3: Enter Deposit Amount
            double depositAmount;
            while (true) {
                System.out.print("Enter Amount to Deposit (enter '0' to exit): ");
                String input = scanner.nextLine().trim();

                if (input.equals("0")) {
                    System.out.println("Deposit process canceled.");
                    return;
                }

                try {
                    depositAmount = Double.parseDouble(input);
                    if (depositAmount > 0) {
                        break;
                    } else {
                        System.out.println("❌ Amount must be greater than zero.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Invalid input. Please enter a numeric amount.");
                }
            }

            // Step 4: Perform Currency Conversion
            double convertedAmount = convertCurrency(depositAmount, depositCurrency, accountCurrency);

            // Step 5: Update Account Balance
            String updateQuery = "UPDATE Account SET Balance = Balance + ? WHERE AccountNumber = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setDouble(1, convertedAmount);
                updateStmt.setString(2, selectedAccount);

                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("✅ Deposit Successful: " + depositAmount + " " + depositCurrency +
                            " deposited into account " + selectedAccount);

                    // Step 6: Insert into accounthistory
                    String insertHistoryQuery = "INSERT INTO accounthistory (SourceAccount, SourceName, TransactionType, " +
                            "Currency, Amount, TransactionDate) VALUES (?, ?, 'Deposit', ?, ?, ?)";
                    try (PreparedStatement historyStmt = conn.prepareStatement(insertHistoryQuery)) {
                        historyStmt.setString(1, selectedAccount);
                        historyStmt.setString(2, accountName);
                        historyStmt.setString(3, depositCurrency);
                        historyStmt.setDouble(4, depositAmount); // Log original amount
                        historyStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                        historyStmt.executeUpdate();
                    }
                } else {
                    System.out.println("❌ Deposit failed. Please try again.");
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
        }
    }

    // Method for Currency Conversion
    private double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        if (fromCurrency.equals("USD") && toCurrency.equals("KHR")) {
            return amount * 4000; // USD to KHR
        } else if (fromCurrency.equals("KHR") && toCurrency.equals("USD")) {
            return amount / 4000; // KHR to USD
        }

        return amount; // No conversion if invalid scenario
    }
}
