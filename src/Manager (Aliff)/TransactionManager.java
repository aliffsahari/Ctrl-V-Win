import java.util.Locale;

/**
 * Stores all transactions in a plain Transaction[] array with a separate
 * counter. Transactions are only ever added (never deleted), so the array
 * is also the full history in the order things happened.
 *
 * Owner: Data & Storage Developer.
 */
public class TransactionManager {

    /** Maximum number of transactions the array can hold. */
    public static final int MAX_TRANSACTIONS = 2000;

    private Transaction[] transactions;
    private int transactionCount;

    /** Creates an empty manager. */
    public TransactionManager() {
        transactions = new Transaction[MAX_TRANSACTIONS];
        transactionCount = 0;
    }

    /**
     * Adds a transaction at the end of the array.
     * @param t the transaction to record
     * @return false if it is null or the array is full
     */
    public boolean addTransaction(Transaction t) {
        if (t == null || transactionCount >= MAX_TRANSACTIONS) {
            return false;
        }
        transactions[transactionCount] = t;
        transactionCount++;
        return true;
    }

    /**
     * Finds every transaction where the account is the sender or receiver.
     * @param accountId the account ID to look for
     * @return a new array holding exactly the matches (length 0 if none)
     */
    public Transaction[] findByAccount(String accountId) {
        int matches = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (involves(transactions[i], accountId)) {
                matches++;
            }
        }
        Transaction[] result = new Transaction[matches];
        int next = 0;
        for (int i = 0; i < transactionCount; i++) {
            if (involves(transactions[i], accountId)) {
                result[next] = transactions[i];
                next++;
            }
        }
        return result;
    }

    /** True if the account is the sender or the receiver of t. */
    private boolean involves(Transaction t, String accountId) {
        if (accountId == null) {
            return false;
        }
        return accountId.equalsIgnoreCase(t.getSenderId())
            || accountId.equalsIgnoreCase(t.getReceiverId());
    }

    /** @return how many transactions are stored */
    public int getCount() {
        return transactionCount;
    }

    /** @return true when no more transactions can be added */
    public boolean isFull() {
        return transactionCount >= MAX_TRANSACTIONS;
    }

    /**
     * Returns the transaction at a position, for loops in other classes.
     * @param index position from 0 to getCount() - 1
     * @return the transaction, or null if the index is out of range
     */
    public Transaction getAt(int index) {
        if (index < 0 || index >= transactionCount) {
            return null;
        }
        return transactions[index];
    }

    /**
     * Creates the next transaction ID, e.g. T00001, T00002...
     * Uses the highest number already stored, so it stays correct after
     * the data is reloaded from file.
     * @return a new unused ID
     */
    public String generateNewId() {
        int max = 0;
        for (int i = 0; i < transactionCount; i++) {
            String id = transactions[i].getTransactionId();
            if (id != null && id.length() > 1 && id.toUpperCase().startsWith("T")) {
                try {
                    int number = Integer.parseInt(id.substring(1));
                    if (number > max) {
                        max = number;
                    }
                } catch (NumberFormatException e) {
                    // not a T + number ID: ignore it
                }
            }
        }
        return "T" + String.format(Locale.US, "%05d", max + 1);
    }

    /**
     * Total money the account has paid out: amount plus fee of every
     * successful PAYMENT or TRANSFER where it is the sender.
     * @param accountId the sender account ID
     * @return the total spent
     */
    public double totalSpent(String accountId) {
        double total = 0;
        for (int i = 0; i < transactionCount; i++) {
            Transaction t = transactions[i];
            if (accountId != null
                    && accountId.equalsIgnoreCase(t.getSenderId())
                    && "SUCCESS".equalsIgnoreCase(t.getStatus())
                    && (t.getType().equalsIgnoreCase("PAYMENT")
                        || t.getType().equalsIgnoreCase("TRANSFER"))) {
                total += t.getAmount() + t.getFee();
            }
        }
        return total;
    }

    /**
     * Total amount the account has received from successful PAYMENT,
     * TRANSFER or REFUND transactions where it is the receiver.
     * @param accountId the receiver account ID
     * @return the total received (before any receiving fee)
     */
    public double totalReceived(String accountId) {
        double total = 0;
        for (int i = 0; i < transactionCount; i++) {
            Transaction t = transactions[i];
            if (accountId != null
                    && accountId.equalsIgnoreCase(t.getReceiverId())
                    && "SUCCESS".equalsIgnoreCase(t.getStatus())
                    && !t.getType().equalsIgnoreCase("TOPUP")) {
                total += t.getAmount();
            }
        }
        return total;
    }

    /** Removes every transaction (used by FileManager before loading). */
    public void clear() {
        for (int i = 0; i < transactionCount; i++) {
            transactions[i] = null;
        }
        transactionCount = 0;
    }
}
