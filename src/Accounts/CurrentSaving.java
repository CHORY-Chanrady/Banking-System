package Accounts;

public class CurrentSaving extends Account {

    public CurrentSaving(String accountNumber, String accountName, String currency, double balance, double interestRate, String creationDate, int customerId) {
        // Call the superclass constructor with all required parameters
        super(accountNumber, accountName, currency, balance, interestRate, creationDate, customerId, 0, "CurrentSaving");
    }

    @Override
    public void displayAccountDetails() {
        System.out.println("\nCurrent Saving Account Details:");
        System.out.printf("Account Number: %s\n", accountNumber);
        System.out.printf("Account Name: %s\n", accountName);
        System.out.printf("Currency: %s\n", currency);
        System.out.printf("Balance: %.2f\n", balance);
        System.out.printf("Interest Rate: %.2f%%\n", interestRate);
        System.out.printf("Creation Date: %s\n", creationDate);
        System.out.printf("Customer ID: %d\n", customerId);
        System.out.printf("Account Type: %s\n", getAccountType());
    }

    @Override
    public int getPeriod() {
        return 0; // Current Saving accounts typically do not have a period
    }

    @Override
    public String getAccountType() {
        return "Current Saving";
    }
}
