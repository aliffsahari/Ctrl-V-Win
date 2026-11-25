/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.insufficientfundexception;

/**
 *
 * @author user
 */
public abstract class Account {
    private String accountId;
    private String name;
    private String pin;
    private double balance;
    private boolean frozen;
    
    //constructor
    public Account(String accountId, String name, String pin, double balance, boolean frozen){
        this.accountId = accountId;
        this.name = name;
        this.pin = pin;
        this.balance = roundMoney(balance);
        this.frozen = frozen;
    }
    
//    method getter
    public String getAccountId(){
        return accountId;
    }
    public String getName(){
        return name;
    }
    public String getPin(){
        return pin;
    }
    public double getBalance(){
        return balance;
    }
    public boolean isFrozen(){
        return frozen;
    }
    
    
    /**
     * Verifies whether the entered PIN matches the account PIN.
     *
     * @param input the PIN entered by the user
     * @return true if PIN matches, false otherwise
     */
    public boolean checkPin(String input){
        return input != null && this.pin.equals(input);
    }
    
    /**
     * Adds funds to the account balance.
     *
     * @param amount amount to top up in RM
     * @throws IllegalArgumentException if amount is 0 or negative
     */
    public void topUp(double amount) throws IllegalArgumentException{
        if (amount <= 0){
            throw new IllegalArgumentException("Top-up amount must be greater than RM 0.00.");
        }
        credit(amount);
    }
    
    //freeze account
    public void freeze(){
        this.frozen = true;
    }
    
    //unfreeze account
    public void unfreeze(){
        this.frozen = false;
    }
    
    /**
     * Deducts a specified amount from the balance if sufficient funds exist.
     *
     * @param amount amount to deduct in RM
     * @throws InsufficientFundsException if balance is lower than the amount
     */protected void deduct(double amount) throws InsufficientFundsException{
         if(this.balance<amount){
             throw new InsufficientFundsException(String.format("Insufficient balance. Available: RM %.2f, Required: RM %.2f", this.balance, amount)); 
         }
         this.balance = roundMoney(this.balance - amount);
     }
     
     /**
     * Rounds a monetary value to 2 decimal places.
     *
     * @param value the raw double value
     * @return value rounded to 2 decimal places
     */
     protected void credit(double amount){
         this.balance = roundMoney(this.balance + amount);
     }
     
     /**
     * Rounds a monetary value to 2 decimal places.
     *
     * @param value the raw double value
     * @return value rounded to 2 decimal places
     */
    protected static double roundMoney(double value){
        return Math.round(value * 100)/100.0;
    } 
    
    /**
     * Calculates the transaction fee based on subclass rules.
     *
     * @param amount transaction amount
     * @return calculated fee in RM
     */
    public abstract double calculateFee(double amount);
    
    /**
     * Processes a payment or transfer from this account to a receiver account.
     *
     * @param receiver the recipient Account
     * @param amount   the amount to pay in RM
     * @throws InsufficientFundsException  if balance cannot cover the payment
     * @throws DailyLimitExceededException if the student daily limit is exceeded
     * @throws AccountFrozenException      if the account is frozen
     */
    public abstract void pay( Account receiver, double amount)
            throws InsufficientFundsException, DailyLimitExceededException, AccountFrozenException;
    
    public abstract String getAccountType();
    
    public abstract String toFileString();
    
    /**
     * Returns a formatted summary of the account for console menus.
     *
     * @return formatted account details
     */
    @Override
    public String toString(){
        String status = frozen ? "FROZEN" : "ACTIVE";
        return String.format("[%s] %s (%s) | Balance: RM %.2f | Status: %s", getAccountType(), name, accountId, balance, status);
    }
}
