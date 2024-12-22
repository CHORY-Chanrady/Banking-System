package Accounts;

import java.time.LocalDate;

public class FixedSaving extends Account {
    private double interestRate;
    private int period; // This is an int field, used for the period of the saving in months.

    // FixedSaving constructor
    public FixedSaving(String accountNumber, String accountName, String currency, double balance,
                       double interestRate, int period, String creationDate, int customerId) {
        super(accountNumber, accountName, currency, balance, customerId, creationDate);
        this.interestRate = interestRate;
        this.period = period;
    }


    // Getter and Setter for interestRate and period
    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    // Override displayAccountDetails() if needed
    @Override
    public void displayAccountDetails() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Account Name: " + accountName);
        System.out.println("Currency: " + currency);
        System.out.println("Balance: " + balance);
        System.out.println("Interest Rate: " + interestRate + "%");
        System.out.println("Period: " + period + " months");
        System.out.println("Creation Date: " + creationDate);
        System.out.println("Account Type: " + accountType);
    }

}
