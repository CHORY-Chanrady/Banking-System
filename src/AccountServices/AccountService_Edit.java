package AccountServices;

import Connection.DatabaseConnection;
import InformationServices.ShowAccountService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AccountService_Edit {
    private final Scanner scanner;

    public AccountService_Edit(Scanner scanner) {
        this.scanner = scanner;
    }

    public void editAccount() {
        if (scanner.hasNextLine()) {
            scanner.nextLine(); // Consume any leftover newline character
        }

        // Initialize ShowAccountService
        ShowAccountService showallaccounts = new ShowAccountService(scanner);
        showallaccounts.showAllAccounts();

        while (true) {
            System.out.print("Enter Account Number to edit (or type 'cancel' to exit): ");
            String accountNumber = scanner.nextLine().trim();

            // Allow user to exit
            if (accountNumber.equalsIgnoreCase("cancel")) {
                System.out.println("Edit operation canceled.");
                return;
            }

            if (accountNumber.isEmpty()) {
                System.out.println("Account Number cannot be empty.");
                continue; // Prompt user to enter the account number again
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "SELECT * FROM Account WHERE AccountNumber = ?";
                PreparedStatement selectStmt = conn.prepareStatement(query);
                selectStmt.setString(1, accountNumber);

                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    boolean continueEditing = true;
                    double currentBalance = rs.getDouble("Balance"); // Fetch current balance
                    String currentCurrency = rs.getString("Currency"); // Fetch current currency

                    while (continueEditing) {
                        System.out.println("Account found. Current details:");
                        System.out.println("1. Account Type: " + rs.getString("AccountType"));
                        System.out.println("2. Currency: " + currentCurrency);
                        System.out.println("3. Exit");

                        System.out.print("\nWhich detail would you like to edit? (1 = Account Type, 2 = Currency, 3 = Exit): ");
                        int choice = -1;
                        if (scanner.hasNextInt()) {
                            choice = scanner.nextInt();
                            scanner.nextLine(); // Consume the newline
                        } else {
                            System.out.println("Invalid input. Please enter 1, 2, or 3.");
                            scanner.nextLine(); // Consume invalid input
                            continue; // Prompt user to re-enter choice
                        }

                        if (choice == 3) {
                            System.out.println("Edit operation canceled.");
                            return;
                        }

                        String columnToEdit = null;
                        switch (choice) {
                            case 1 -> columnToEdit = "AccountType";
                            case 2 -> columnToEdit = "Currency";
                            default -> {
                                System.out.println("Invalid choice. Exiting edit operation.");
                                return;
                            }
                        }

                        String newValue = null;
                        if (columnToEdit.equals("AccountType")) {
                            newValue = getNewAccountType();
                        } else if (columnToEdit.equals("Currency")) {
                            newValue = getNewCurrency();

                            // Check if the currency is changing
                            if (!currentCurrency.equals(newValue)) {
                                // Convert the balance based on the new currency
                                currentBalance = convertCurrency(currentBalance, currentCurrency, newValue);
                            }
                        }

                        if (newValue == null) {
                            System.out.println("Invalid input. Edit operation canceled.");
                            return;
                        }

                        // Update currency and balance in the database
                        String updateQuery = "UPDATE Account SET " + columnToEdit + " = ?, Balance = ? WHERE AccountNumber = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, newValue);
                            updateStmt.setDouble(2, currentBalance);
                            updateStmt.setString(3, accountNumber);

                            int rowsUpdated = updateStmt.executeUpdate();
                            if (rowsUpdated > 0) {
                                System.out.println("Account details updated successfully.");
                                System.out.print("Would you like to edit another detail? (yes/no): ");
                                String response = scanner.nextLine().trim().toLowerCase();
                                if (response.equals("no")) {
                                    continueEditing = false; // Exit the loop if user chooses not to continue
                                }
                            } else {
                                System.out.println("Failed to update account details.");
                            }
                        }
                    }
                } else {
                    System.out.println("Account not found. Please try again.");
                }
            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private String getNewAccountType() {
        System.out.println("Choose Account Type:");
        System.out.println("C = Current Saving");
        System.out.println("F = Fixed Saving");
        System.out.println("P = Payroll");
        System.out.println("L = Loan");

        while (true) {
            System.out.print("Enter your choice (C, F, P, L): ");
            String input = scanner.nextLine().trim().toUpperCase();

            switch (input) {
                case "C":
                    return "Current Saving";
                case "F":
                    return "Fixed Saving";
                case "P":
                    return "Payroll";
                case "L":
                    return "Loan";
                default:
                    System.out.println("Invalid input. Please choose a valid account type.");
            }
        }
    }

    private String getNewCurrency() {
        System.out.println("Choose Currency:");
        System.out.println("U = USD");
        System.out.println("K = KHR");

        while (true) {
            System.out.print("Enter your choice (U, K): ");
            String input = scanner.nextLine().trim().toUpperCase();

            switch (input) {
                case "U":
                    return "USD";
                case "K":
                    return "KHR";
                default:
                    System.out.println("Invalid input. Please choose a valid currency.");
            }
        }
    }

    // Currency conversion method
    private double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        double conversionRate = 1.0;

        // Define conversion rates between USD and KHR
        if (fromCurrency.equals("USD") && toCurrency.equals("KHR")) {
            conversionRate = 4000; // 1 USD = 4000 KHR (example rate)
        } else if (fromCurrency.equals("KHR") && toCurrency.equals("USD")) {
            conversionRate = 1 / 4000.0; // 1 KHR = 0.00025 USD
        }

        return amount * conversionRate;
    }
}

