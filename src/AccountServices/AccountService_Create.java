package AccountServices;

import Accounts.*;
import Connection.DatabaseConnection;
import InformationServices.ShowCustomerService;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class AccountService_Create {
    private final Scanner scanner;
    private final ShowCustomerService showCustomerService;

    public AccountService_Create(Scanner scanner) {
        this.scanner = scanner;
        this.showCustomerService = new ShowCustomerService(scanner);
    }

    public void createAccount() {
        int choice = -1; // Initializing choice to -1 as a flag value.
        do {
            System.out.println("\n===== Create Account Menu =====");
            System.out.println("1. Current Saving Account");
            System.out.println("2. Fixed Saving Account");
            System.out.println("3. Loan Account");
            System.out.println("4. Payroll Account");
            System.out.println("5. Back to Main Menu");
            System.out.print("Choose an account type: ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Consume the invalid input
                continue; // Skip the rest of the loop and ask for input again
            }

            // Check if user wants to exit
            if (choice == 5) {
                System.out.println("Returning to Main Menu...");
                return; // Exit method to return to the main menu
            }

            String accountType = getAccountType(choice); // Assuming this method accepts choice
            if (accountType == null) {
                System.out.println("Invalid choice or operation cancelled. Returning to Main Menu...");
                return;
            }

            showCustomerService.showAllCustomers();
            System.out.println("\nType 'cancel' in any field to exit");

            int customerId;
            while (true) {
                customerId = getCustomerId();
                if (customerId == -1) {
                    System.out.println("Operation cancelled. Returning to Main Menu...");
                    return;
                }

                String accountName = getCustomerNameById(customerId);
                if (accountName != null) {
                    break; // Exit the loop if customer is found
                } else {
                    System.out.println("Customer not found. Please try again.");
                }
            }

            // Count associated accounts for the customer
            int associatedAccountsCount = countAssociatedAccounts(customerId);
            System.out.println("Customer has " + associatedAccountsCount + " associated accounts.");

            String accountName = getCustomerNameById(customerId);
            System.out.println("Account Name: " + accountName);
            String accountNumber = generateUniqueAccountNumber();
            System.out.println("Account Number: " + accountNumber);

            String currency = getValidStringInput("Enter Currency (USD/KHR): ", "USD", "KHR");
            if (currency == null) {
                System.out.println("Operation cancelled. Returning to Main Menu...");
                return;
            }

            double balance = getValidDoubleInput("Enter Initial Balance: ");
            if (balance == -1) {
                System.out.println("Operation cancelled. Returning to Main Menu...");
                return;
            }

            Account account = createAccountObject(accountType, accountNumber, accountName, currency, balance, customerId);
            if (account != null) {
                System.out.println("\nAccount created successfully!");
                account.displayAccountDetails();

                // Insert the new account into the database
                if (insertAccountIntoDatabase(account)) {
                    System.out.println("Account information inserted into the database successfully.");
                } else {
                    System.out.println("Failed to insert account into the database.");
                }
            }
        } while (choice != 5); // Loop will exit when choice is 5 (Back to Main Menu)
    }

    private int countAssociatedAccounts(int customerId) {
        String query = "SELECT COUNT(*) FROM Account WHERE CustomerID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // Return the count of associated accounts
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        return 0; // Return 0 if there is an error or no accounts found
    }

    private boolean insertAccountIntoDatabase(Account account) {
        String query = "INSERT INTO Account (AccountNumber, AccountName, Currency, Balance, CustomerID, CreationDate, AccountType, InterestRate, Period) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, account.getAccountNumber());
            stmt.setString(2, account.getAccountName());
            stmt.setString(3, account.getCurrency());
            stmt.setDouble(4, account.getBalance());
            stmt.setInt(5, account.getCustomerId());

            Timestamp creationDate = Timestamp.valueOf(LocalDateTime.now());
            stmt.setString(6, creationDate.toString());

            stmt.setString(7, account.getAccountType());

            // Set additional fields based on account type (e.g., interest rate and period)
            if (account instanceof CurrentSaving) {
                stmt.setDouble(8, ((CurrentSaving) account).getInterestRate());
                stmt.setNull(9, java.sql.Types.INTEGER); // Set to NULL if not applicable
            } else if (account instanceof FixedSaving) {
                stmt.setDouble(8, ((FixedSaving) account).getInterestRate());
                stmt.setInt(9, ((FixedSaving) account).getPeriod());
            } else if (account instanceof Loan) {
                stmt.setDouble(8, ((Loan) account).getInterestRate());
                stmt.setInt(9, ((Loan) account).getPeriod()); // Include period for loan
            } else if (account instanceof Payroll) {
                stmt.setDouble(8, ((Payroll) account).getInterestRate());
                stmt.setNull(9, java.sql.Types.INTEGER);
            } else {
                stmt.setNull(8, java.sql.Types.DOUBLE);
                stmt.setNull(9, java.sql.Types.INTEGER);
            }

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error during account insertion: " + e.getMessage());
            return false;
        }
    }

    private String getAccountType(int choice) {
        return switch (choice) {
            case 1 -> "Current Saving";
            case 2 -> "Fixed Saving";
            case 3 -> "Loan";
            case 4 -> "Payroll";
            default -> null;
        };
    }

    private int getCustomerId() {
        while (true) {
            System.out.print("Enter Customer ID: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) {
                return -1; // Indicate cancellation
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric Customer ID or 'cancel' to exit.");
            }
        }
    }

    private double getValidDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) {
                return -1; // Indicate cancellation
            }
            try {
                double value = Double.parseDouble(input);
                if (value >= 0) {
                    return value;
                } else {
                    System.out.println("Balance must be non-negative.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a valid balance.");
            }
        }
    }

    private String getValidStringInput(String prompt, String... validValues) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equalsIgnoreCase("cancel")) {
                return null; // Indicate cancellation
            }
            if (validValues.length == 0 || Arrays.asList(validValues).contains(input)) {
                return input;
            } else {
                System.out.println("Invalid input. Please enter one of the valid options: " + Arrays.toString(validValues));
            }
        }
    }

    private Account createAccountObject(String accountType, String accountNumber, String accountName, String currency, double balance, int customerId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String creationDate = LocalDate.now().format(formatter); // Format the current date as DD-MM-YYYY
        return switch (accountType) {
            case "Current Saving" -> new CurrentSaving(accountNumber, accountName, currency, balance, 0.5, creationDate, customerId);
            case "Fixed Saving" -> {
                int period = getValidIntInput("Enter Fixed Saving Period (months): ", 1, 120);
                if (period == -1) {
                    yield null; // Exit if user cancels
                }
                yield new FixedSaving(accountNumber, accountName, currency, balance, 1.0, period, creationDate, customerId);
            }
            case "Loan" -> {
                int period = getValidIntInput("Enter Loan Period (months): ", 1, 120);
                if (period == -1) {
                    yield null; // Exit if user cancels
                }
                yield new Loan(accountNumber, accountName, currency, balance, 1.2, period, creationDate, customerId, 12, "LoanAccount");
            }
            case "Payroll" -> new Payroll(accountNumber, accountName, currency, balance, 0, creationDate, customerId, 12, "PayrollAccount");
            default -> null;
        };
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        int accountNumber = 100000000 + random.nextInt(900000000); // Generates a random 9-digit number
        return String.format("%09d", accountNumber); // Format as a 9-digit number with leading zeros
    }

    private String getCustomerNameById(int customerId) {
        String query = "SELECT CustomerName FROM Customer WHERE CustomerID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("CustomerName");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        return null; // Return null if customer not found
    }
    private int getValidIntInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) {
                return -1; // Indicate cancellation
            }
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println("Input must be between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

}
