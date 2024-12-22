package Accounts;

public class Loan extends Account {

    private int loanPeriod;  // Loan period in months or years

    public Loan(String accountNumber, String accountName, String currency, double balance,
                double interestRate, int loanPeriod, String creationDate, int customerId,
                int someAdditionalParameter, String anotherParameter) {
        super(accountNumber, accountName, currency, balance, interestRate, creationDate, customerId, someAdditionalParameter, anotherParameter);
        this.loanPeriod = loanPeriod;
    }

    @Override
    public void displayAccountDetails() {
        System.out.println("\nLoan Account Details:");
        System.out.printf("Account Number: %s\n", accountNumber);
        System.out.printf("Account Name: %s\n", accountName);
        System.out.printf("Currency: %s\n", currency);
        System.out.printf("Balance: %.2f\n", balance);
        System.out.printf("Interest Rate: %.2f%%\n", interestRate);
        System.out.printf("Loan Period: %d months\n", loanPeriod);
        System.out.printf("Creation Date: %s\n", creationDate);
        System.out.printf("Customer ID: %d\n", customerId);
    }

    @Override
    public int getPeriod() {
        return loanPeriod;  // Return the actual loan period
    }

    @Override
    public String getAccountType() {
        return "Loan";  // Return the account type as "Loan"
    }
}
