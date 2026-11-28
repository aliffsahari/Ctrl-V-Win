import java.time.LocalDate;
import java.util.Locale;

/**
 * Represents a student e-wallet account with a daily spending limit and peer-to-peer transfer support.
 */
public class StudentAccount extends Account {

    private double dailyLimit;
    private double spentToday;
    private String lastSpendDate;

    public static final double DEFAULT_DAILY_LIMIT = 500.00;
    public static final double TRANSFER_FEE = 0.50;

    /**
     * Constructs a StudentAccount with the specified details and daily spending limit.
     *
     * @param id         unique student account ID (e.g., S0001)
     * @param name       student's full name
     * @param pin        security PIN
     * @param balance    initial wallet balance
     * @param frozen     account freeze status
     * @param dailyLimit maximum daily spending limit in RM
     */
    public StudentAccount(String id, String name, String pin, double balance, boolean frozen, double dailyLimit) {
        super(id, name, pin, balance, frozen);
        this.dailyLimit = (dailyLimit > 0) ? roundMoney(dailyLimit) : DEFAULT_DAILY_LIMIT;
        this.spentToday = 0.0;
        this.lastSpendDate = LocalDate.now().toString();
    }

    /**
     * Returns the daily spending limit.
     *
     * @return daily limit in RM
     */
    public double getDailyLimit() {
        return dailyLimit;
    }

    /**
     * Returns the amount spent today, resetting if a new calendar day has begun.
     *
     * @return amount spent today in RM
     */
    public double getSpentToday() {
        resetDailyIfNewDay();
        return spentToday;
    }

    /**
     * Returns the remaining daily spending limit for today.
     *
     * @return remaining daily limit in RM
     */
    public double getRemainingLimit() {
        resetDailyIfNewDay();
        return roundMoney(dailyLimit - spentToday);
    }

    /**
     * Resets spentToday to zero if the calendar date has changed since the last transaction.
     */
    private void resetDailyIfNewDay() {
        String today = LocalDate.now().toString();
        if (!today.equals(this.lastSpendDate)) {
            this.spentToday = 0.0;
            this.lastSpendDate = today;
        }
    }

    /**
     * Calculates the transaction fee for merchant payments (RM 0.00 for students).
     *
     * @param amount payment amount
     * @return fee in RM (0.0)
     */
    @Override
    public double calculateFee(double amount) {
        return 0.0;
    }

    /**
     * Pays a merchant or recipient account, enforcing freeze status, daily limit, and balance checks.
     *
     * @param receiver the recipient Account
     * @param amount   the amount to pay in RM
     * @throws InsufficientFundsException  if balance is insufficient
     * @throws DailyLimitExceededException if payment exceeds remaining daily limit
     * @throws AccountFrozenException      if sender or receiver account is frozen
     */
    @Override
    public void pay(Account receiver, double amount)
            throws InsufficientFundsException, DailyLimitExceededException, AccountFrozenException {
        if (this.isFrozen()) {
            throw new AccountFrozenException("Your student account (" + getAccountId() + ") is frozen.");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver account cannot be null.");
        }
        if (receiver.isFrozen()) {
            throw new AccountFrozenException("Receiver account (" + receiver.getAccountId() + ") is frozen.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than RM 0.00.");
        }

        resetDailyIfNewDay();

        if (roundMoney(spentToday + amount) > dailyLimit) {
            throw new DailyLimitExceededException(
                String.format(Locale.US, "Daily limit of RM %.2f exceeded. Remaining limit: RM %.2f",
                    dailyLimit, getRemainingLimit())
            );
        }

        deduct(amount);
        this.spentToday = roundMoney(this.spentToday + amount);

        if (receiver instanceof MerchantAccount) {
            ((MerchantAccount) receiver).receivePayment(amount);
        } else {
            receiver.credit(amount);
        }
    }

    /**
     * Transfers funds to another student account with a flat transfer fee.
     *
     * @param receiver the recipient StudentAccount
     * @param amount   the transfer amount in RM
     * @throws InsufficientFundsException  if balance cannot cover amount + transfer fee
     * @throws DailyLimitExceededException if total debit exceeds remaining daily limit
     * @throws AccountFrozenException      if sender or receiver account is frozen
     */
    public void transfer(StudentAccount receiver, double amount)
            throws InsufficientFundsException, DailyLimitExceededException, AccountFrozenException {
        if (this.isFrozen()) {
            throw new AccountFrozenException("Your student account (" + getAccountId() + ") is frozen.");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver student account cannot be null.");
        }
        if (receiver.isFrozen()) {
            throw new AccountFrozenException("Receiver account (" + receiver.getAccountId() + ") is frozen.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than RM 0.00.");
        }

        resetDailyIfNewDay();

        double totalDebit = roundMoney(amount + TRANSFER_FEE);
        if (roundMoney(spentToday + totalDebit) > dailyLimit) {
            throw new DailyLimitExceededException(
                String.format(Locale.US, "Daily limit of RM %.2f exceeded (including RM %.2f fee). Remaining: RM %.2f",
                    dailyLimit, TRANSFER_FEE, getRemainingLimit())
            );
        }

        deduct(totalDebit);
        this.spentToday = roundMoney(this.spentToday + totalDebit);
        receiver.credit(amount);
    }

    /**
     * Returns the account type identifier.
     *
     * @return "STUDENT"
     */
    @Override
    public String getAccountType() {
        return "STUDENT";
    }

    /**
     * Formats this student account as a pipe-delimited string for accounts.txt.
     *
     * @return formatted student account line
     */
    @Override
    public String toFileString() {
        return String.format(Locale.US, "STUDENT|%s|%s|%s|%.2f|%b|%.2f",
                getAccountId(), getName(), getPin(), getBalance(), isFrozen(), dailyLimit);
    }
}