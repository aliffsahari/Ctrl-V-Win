/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */



/**
 * Represents a merchant e-wallet account that receives customer payments and issues refunds.
 */
public class MerchantAccount extends Account {

    private String businessName;
    public static final double RECEIVE_FEE_RATE = 0.015;

    /**
     * Constructs a MerchantAccount with the specified details and business name.
     *
     * @param id           unique merchant account ID (e.g., M0001)
     * @param name         merchant owner's name
     * @param pin          security PIN
     * @param balance      initial wallet balance
     * @param frozen       account freeze status
     * @param businessName registered business or shop name
     */
    public MerchantAccount(String id, String name, String pin, double balance, boolean frozen, String businessName) {
        super(id, name, pin, balance, frozen);
        this.businessName = businessName;
    }

    /**
     * Returns the merchant's business name.
     *
     * @return the business name
     */
    public String getBusinessName() {
        return businessName;
    }

    /**
     * Calculates the 1.5% processing fee charged when receiving a payment.
     *
     * @param amount gross payment amount in RM
     * @return calculated fee in RM
     */
    @Override
    public double calculateFee(double amount) {
        return roundMoney(amount * RECEIVE_FEE_RATE);
    }

    /**
     * Processes an outgoing refund or payment from this merchant account with no daily limit.
     *
     * @param receiver the recipient Account
     * @param amount   the refund or payment amount in RM
     * @throws InsufficientFundsException  if merchant balance is insufficient
     * @throws DailyLimitExceededException declared by parent signature (not applicable to merchants)
     * @throws AccountFrozenException      if merchant or receiver account is frozen
     */
    @Override
    public void pay(Account receiver, double amount)
            throws InsufficientFundsException, DailyLimitExceededException, AccountFrozenException {
        if (this.isFrozen()) {
            throw new AccountFrozenException("Merchant account (" + getAccountId() + ") is frozen.");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver account cannot be null.");
        }
        if (receiver.isFrozen()) {
            throw new AccountFrozenException("Receiver account (" + receiver.getAccountId() + ") is frozen.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than RM 0.00.");
        }

        deduct(amount);
        receiver.credit(amount);
    }

    /**
     * Credits a customer payment to the merchant's balance after deducting the 1.5% fee.
     *
     * @param amount gross payment amount received in RM
     */
    public void receivePayment(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than RM 0.00.");
        }
        double fee = calculateFee(amount);
        double netAmount = roundMoney(amount - fee);
        credit(netAmount);
    }

    /**
     * Returns the account type identifier.
     *
     * @return "MERCHANT"
     */
    @Override
    public String getAccountType() {
        return "MERCHANT";
    }

    /**
     * Formats this merchant account as a pipe-delimited string for accounts.txt.
     *
     * @return formatted merchant account line
     */
    @Override
    public String toFileString() {
        return String.format("MERCHANT|%s|%s|%s|%.2f|%b|%s",
                getAccountId(), getName(), getPin(), getBalance(), isFrozen(), businessName);
    }
}
