package Accounts;

public abstract class Account {
    protected String accountNumber;
    protected String accountName;
    protected String currency;
    protected double balance;
    protected double interestRate;
    protected String creationDate;
    protected int customerId;
    protected int period; // Corrected spelling
    protected String accountType;

    // Primary constructor
    public Account(String accountNumber, String accountName, String currency, double balance, double interestRate, String creationDate, int customerId, int period, String accountType) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.currency = currency;
        this.balance = balance;
        this.interestRate = interestRate;
        this.creationDate = creationDate;
        this.customerId = customerId;
        this.period = period; // Corrected spelling
        this.accountType = accountType;
    }

    // Secondary constructor - delegates to the primary constructor
    public Account(String accountNumber, String accountName, String currency, double balance, int customerId, String creationDate) {
        this(accountNumber, accountName, currency, balance, 0.0, creationDate, customerId, 0, "Fixed Saving");
    }

    // Abstract method to be implemented by subclasses
    public abstract void displayAccountDetails();

    // Getters
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getCurrency() {
        return currency;
    }

    public double getBalance() {
        return balance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getPeriod() { // Corrected spelling
        return period;
    }

    public String getAccountType() {
        return accountType;
    }
}
