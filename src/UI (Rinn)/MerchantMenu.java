import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * The menu a merchant sees after logging in: balance, payments received,
 * a sales summary and refunds.
 *
 * Owner: Rinn (Interface and Quality Developer), Group 6.
 */
public class MerchantMenu {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Scanner sc;
    private MerchantAccount user;
    private AccountManager am;
    private TransactionManager tm;

    /**
     * Creates the menu for one logged-in merchant.
     * @param sc the Scanner for keyboard input
     * @param user the merchant who logged in
     * @param am all accounts, used to find the student for a refund
     * @param tm all transactions, used to list and record them
     */
    public MerchantMenu(Scanner sc, MerchantAccount user, AccountManager am, TransactionManager tm) {
        this.sc = sc;
        this.user = user;
        this.am = am;
        this.tm = tm;
    }

    /** Shows the merchant menu until the merchant logs out. */
    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- MERCHANT MENU (" + user.getAccountId() + ", " + user.getBusinessName() + ") ---");
            System.out.println("1. View balance");
            System.out.println("2. Payments received");
            System.out.println("3. Sales summary");
            System.out.println("4. Refund a payment");
            System.out.println("0. Log out");
            int choice = InputValidator.readInt(sc, "Choose: ", 0, 4);
            switch (choice) {
                case 1: viewBalance(); break;
                case 2: viewReceivedPayments(); break;
                case 3: viewSalesSummary(); break;
                case 4: doRefund(); break;
                case 0: back = true; break;
            }
        }
        System.out.println("Logged out.");
    }

    /** Shows the business name, balance and a frozen warning. */
    private void viewBalance() {
        System.out.println("Business: " + user.getBusinessName());
        System.out.printf("Balance: RM %.2f%n", user.getBalance());
        if (user.isFrozen()) {
            System.out.println("Status: FROZEN (refunds are blocked)");
        }
    }

    /** Lists the successful payments that students made to this merchant. */
    private void viewReceivedPayments() {
        Transaction[] list = tm.findByAccount(user.getAccountId());
        int shown = 0;
        for (int i = 0; i < list.length; i++) {
            if (isPaymentToMe(list[i]) && isSuccess(list[i])) {
                shown++;
                System.out.println(shown + ". " + list[i]);
            }
        }
        if (shown == 0) {
            System.out.println("No payments received yet.");
        }
    }

    /** Totals the successful sales, the 1.5% fees and the refunds paid. */
    private void viewSalesSummary() {
        Transaction[] list = tm.findByAccount(user.getAccountId());
        int count = 0;
        double sales = 0, fees = 0, refunds = 0;
        for (int i = 0; i < list.length; i++) {
            Transaction t = list[i];
            if (!isSuccess(t)) {
                continue;
            }
            if (isPaymentToMe(t)) {
                count++;
                sales += t.getAmount();
                fees += t.getFee();
            } else if (t.getType().equals("REFUND") && t.getSenderId().equals(user.getAccountId())) {
                refunds += t.getAmount();
            }
        }
        System.out.println("Payments received: " + count);
        System.out.printf("Total sales:  RM %.2f%n", sales);
        System.out.printf("Total fees:   RM %.2f%n", fees);
        System.out.printf("Refunds paid: RM %.2f%n", refunds);
        System.out.printf("Net income:   RM %.2f%n", sales - fees - refunds);
    }

    /** Gives back the money of one payment, after the merchant confirms. A failed refund is recorded as FAILED. */
    private void doRefund() {
        String id = InputValidator.readNonEmpty(sc, "Transaction ID to refund (e.g. T00002): ").toUpperCase();
        Transaction t = findTransaction(id);
        if (t == null || !isPaymentToMe(t)) {
            System.out.println("No payment to you with ID " + id + ".");
            return;
        }
        if (!isSuccess(t)) {
            System.out.println("This payment cannot be refunded (status: " + t.getStatus() + ").");
            return;
        }
        if (alreadyRefunded(t)) {
            System.out.println("This payment has already been refunded.");
            return;
        }
        Account student = am.findById(t.getSenderId());
        if (student == null) {
            System.out.println("The student account " + t.getSenderId() + " no longer exists.");
            return;
        }
        double amount = t.getAmount();
        System.out.printf("Refund RM %.2f to %s?%n", amount, student.getName());
        if (!InputValidator.readYesNo(sc, "Confirm (y/n): ")) {
            System.out.println("Refund cancelled.");
            return;
        }
        try {
            user.pay(student, amount);
            record("REFUND", student.getAccountId(), amount, "SUCCESS");
            System.out.printf("Refund done. New balance: RM %.2f%n", user.getBalance());
        } catch (InsufficientFundsException e) {
            failed(student.getAccountId(), amount, "Refund failed. " + e.getMessage());
        } catch (DailyLimitExceededException e) {
            failed(student.getAccountId(), amount, "Refund failed. " + e.getMessage());
        } catch (AccountFrozenException e) {
            failed(student.getAccountId(), amount, "Refund failed. " + e.getMessage());
        }
    }

    /** True when t is a payment that a student made to this merchant (any status). */
    private boolean isPaymentToMe(Transaction t) {
        return t.getType().equals("PAYMENT") && user.getAccountId().equalsIgnoreCase(t.getReceiverId());
    }

    /** True when the transaction went through. */
    private boolean isSuccess(Transaction t) {
        return "SUCCESS".equals(t.getStatus());
    }

    /**
     * Stops the same money from being refunded twice. Transaction has no setter,
     * so a refunded payment keeps its SUCCESS status. Instead this counts the
     * successful payments and refunds of the same amount between this merchant
     * and this student: once the refunds reach the payments, nothing is left to refund.
     */
    private boolean alreadyRefunded(Transaction payment) {
        String me = user.getAccountId();
        String student = payment.getSenderId();
        int payments = 0;
        int refunds = 0;
        for (int i = 0; i < tm.getCount(); i++) {
            Transaction t = tm.getAt(i);
            if (!isSuccess(t) || Math.abs(t.getAmount() - payment.getAmount()) >= 0.005) {
                continue;   // failed, or a different amount
            }
            if (t.getType().equals("PAYMENT") && student.equalsIgnoreCase(t.getSenderId())
                    && me.equalsIgnoreCase(t.getReceiverId())) {
                payments++;
            } else if (t.getType().equals("REFUND") && me.equalsIgnoreCase(t.getSenderId())
                    && student.equalsIgnoreCase(t.getReceiverId())) {
                refunds++;
            }
        }
        return refunds >= payments;
    }

    /** Finds a transaction by its ID with a loop, or returns null. */
    private Transaction findTransaction(String id) {
        for (int i = 0; i < tm.getCount(); i++) {
            Transaction t = tm.getAt(i);
            if (t.getTransactionId().equalsIgnoreCase(id)) {
                return t;
            }
        }
        return null;
    }

    /** Prints why a refund failed and keeps a FAILED record of it. */
    private void failed(String to, double amount, String message) {
        System.out.println(message);
        record("REFUND", to, amount, "FAILED");
    }

    /** Records one refund by this merchant in the transaction list (refunds have no fee). */
    private void record(String type, String to, double amount, String status) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        Transaction t = new Transaction(tm.generateNewId(), type, user.getAccountId(), to, amount, 0.0, time, status);
        if (!tm.addTransaction(t)) {
            System.out.println("Warning: the transaction list is full, so this record was not kept.");
        }
    }
}
