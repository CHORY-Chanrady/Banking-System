import AccountServices.AccountService_Edit;
import AccountServices.AccountService_Delete;
import AccountServices.AccountService_Create;
import BankingServices.TransferService; // Import TransferService
import CustomerServices.CustomerService_Create;
import CustomerServices.CustomerService_Delete;
import CustomerServices.CustomerService_Edit;
import BankingServices.DepositService;
import BankingServices.WithdrawService;
import InformationServices.ShowAccountService;
import InformationServices.ShowCustomerService;
import InformationServices.AccountActivityService;
import Authentication.Login;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login loginService = new Login(scanner); // New Login service

        int choice = -1;

        // Step 1: Login Menu
        while (choice != 3) {
            System.out.println("\n===== 🔐 Welcome to the Banking System =====");
            System.out.println("1. Sign In");
            System.out.println("2. Sign Up");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } else {
                System.out.println("Invalid input! Please enter a number.");
                scanner.next();
                continue;
            }

            switch (choice) {
                case 1 -> {
                    if (loginService.signIn()) {
                        showMainMenu(scanner); // Show main menu after successful login
                    }
                }
                case 2 -> loginService.signUp();
                case 3 -> System.out.println("Exiting... Thank you for using the system!");
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void showMainMenu(Scanner scanner) {
        // Initialize services
        CustomerService_Edit customerServiceEdit = new CustomerService_Edit(scanner);
        CustomerService_Create customerServiceCreate = new CustomerService_Create(scanner);
        CustomerService_Delete customerServiceDelete = new CustomerService_Delete(scanner);
        AccountService_Create accountServiceCreate = new AccountService_Create(scanner);
        AccountService_Delete accountServiceDelete = new AccountService_Delete(scanner);
        AccountService_Edit accountServiceEdit = new AccountService_Edit(scanner);
        DepositService depositService = new DepositService(scanner);
        WithdrawService withdrawService = new WithdrawService(scanner);
        ShowCustomerService showCustomerService = new ShowCustomerService(scanner);
        ShowAccountService showAccountService = new ShowAccountService(scanner);
        AccountActivityService accountActivityService = new AccountActivityService(scanner);
        TransferService transferService = new TransferService(scanner); // Initialize TransferService

        int choice = -1;

        // Step 2: Main Menu
        do {
            try {
                System.out.println("\n===== 💱Banking System Menu🏦 =====");
                System.out.println("1. Create Customer");
                System.out.println("2. Delete Customer");
                System.out.println("3. Edit Customer");
                System.out.println("4. Create Account");
                System.out.println("5. Delete Account");
                System.out.println("6. Edit Account");
                System.out.println("7. Deposit");
                System.out.println("8. Withdraw");
                System.out.println("9. Transfer");
                System.out.println("10. Show Customer Information");
                System.out.println("11. Show Account Information");
                System.out.println("12. Show Account Activity");
                System.out.println("13. Logout");
                System.out.print("Enter your choice: ");

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                } else {
                    System.out.println("Invalid input! Please enter a number.");
                    scanner.next(); // Clear invalid input
                    continue;
                }

                switch (choice) {
                    case 1 -> customerServiceCreate.createCustomer();
                    case 2 -> customerServiceDelete.deleteCustomer();
                    case 3 -> customerServiceEdit.editCustomer();
                    case 4 -> accountServiceCreate.createAccount();
                    case 5 -> accountServiceDelete.deleteAccount();
                    case 6 -> accountServiceEdit.editAccount();
                    case 7 -> depositService.deposit();
                    case 8 -> withdrawService.withdraw();
                    case 9 -> transferService.transferMoney(); // Call transferMoney
                    case 10 -> showCustomerService.showCustomerInformation();
                    case 11 -> showAccountService.showAccountsMenu();
                    case 12 -> accountActivityService.showAccountActivitiesMenu();
                    case 13 -> {
                        System.out.print("Are you sure you want to log out? (yes/no): ");
                        String confirm = scanner.nextLine().trim().toLowerCase();
                        if (confirm.equals("yes")) {
                            System.out.println("Logging out... Returning to the login menu.");
                            return;
                        } else {
                            System.out.println("Logout canceled.");
                        }
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Error: Please enter a valid integer.");
                scanner.next(); // Clear invalid input
            }
        } while (choice != 13);
    }
}
