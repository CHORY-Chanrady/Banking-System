package Accounts;

public class Payroll extends Account {

    public Payroll(String accountNumber, String accountName, String currency, double balance,
                   double interestRate, String creationDate, int customerId, int someAdditionalParameter, String anotherParameter) {
        super(accountNumber, accountName, currency, balance, interestRate, creationDate, customerId, someAdditionalParameter, anotherParameter);
    }

    @Override
    public void displayAccountDetails() {
        System.out.println("\nPayroll Account Details:");
        System.out.printf("Account Number: %s\n", accountNumber);
        System.out.printf("Account Name: %s\n", accountName);
        System.out.printf("Currency: %s\n", currency);
        System.out.printf("Balance: %.2f\n", balance);
        System.out.printf("Interest Rate: %.2f%%\n", interestRate);
        System.out.printf("Creation Date: %s\n", creationDate);
        System.out.printf("Customer ID: %d\n", customerId);
    }

    @Override
    public int getPeriod() {
        // Since Payroll accounts typically do not have a loan period, this could return a default value
        return 0;  // or return -1 to indicate no period
    }

    @Override
    public String getAccountType() {
        return "Payroll";  // Return the account type as "Payroll"
    }
}
