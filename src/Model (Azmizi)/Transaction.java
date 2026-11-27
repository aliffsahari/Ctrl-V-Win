

/**
 * Represents a single financial transaction record (top-up, payment, transfer, or refund).
 */
import java.util.Locale;
public class Transaction {

    private String transactionId;
    private String type;
    private String senderId;
    private String receiverId;
    private double amount;
    private double fee;
    private String dateTime;
    private String status;

    /**
     * Constructs a Transaction with all required record fields.
     *
     * @param transactionId unique transaction ID (e.g., T00001)
     * @param type          transaction type (TOPUP, PAYMENT, TRANSFER, REFUND)
     * @param senderId      ID of the sending account
     * @param receiverId    ID of the receiving account
     * @param amount        transaction amount in RM
     * @param fee           transaction fee charged in RM
     * @param dateTime      timestamp of the transaction
     * @param status        status of the transaction (e.g., SUCCESS)
     */
    public Transaction(String transactionId, String type, String senderId, String receiverId,
                       double amount, double fee, String dateTime, String status) {
        this.transactionId = transactionId;
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.fee = fee;
        this.dateTime = dateTime;
        this.status = status;
    }

    /**
     * Returns the transaction ID.
     * @return the transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Returns the transaction type.
     * @return the transaction type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the sender account ID.
     * @return the sender ID
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Returns the receiver account ID.
     * @return the receiver ID
     */
    public String getReceiverId() {
        return receiverId;
    }

    /**
     * Returns the transaction amount.
     * @return the amount in RM
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Returns the transaction fee.
     * @return the fee in RM
     */
    public double getFee() {
        return fee;
    }

    /**
     * Returns the transaction date and time.
     * @return the dateTime string
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Returns the transaction status.
     * @return the status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Formats this transaction as a pipe-delimited line for transactions.txt.
     * @return pipe-delimited transaction record
     */
    public String toFileString() {
        return String.format(Locale.US, "%s|%s|%s|%s|%.2f|%.2f|%s|%s",
                transactionId, type, senderId, receiverId, amount, fee, dateTime, status);
    }
    /**
     * Returns a formatted summary of the transaction for console display.
     * @return formatted transaction string
     */
    @Override
    public String toString() {
        return String.format("[%s] %s | %-8s | From: %s -> To: %s | RM %.2f (Fee: RM %.2f) | %s",
                dateTime, transactionId, type, senderId, receiverId, amount, fee, status);
    }
}