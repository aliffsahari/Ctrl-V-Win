import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * The menu a student sees after logging in: balance, top up, pay a merchant,
 * transfer to another student, history, AI spending advice and a statement.
 * Every action catches the custom exceptions and prints a clear message.
 *
 * Owner: Rinn (Interface & Quality Developer), Group 6.
 */
public class StudentMenu {

    private static final double MAX_TOP_UP = 5000.00;
    private static final double MAX_AMOUNT = 5000.00;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Scanner sc;
    private StudentAccount user;
    private AccountManager am;
    private TransactionManager tm;
    private SpendingAdvisor advisor;
    private ReportGenerator reports;

    /**
     * Creates the menu for one logged-in student.
     * @param sc the Scanner for keyboard input
     * @param user the student who logged in
     * @param am all accounts, used to find merchants and other students
     * @param tm all transactions, used to record and list them
     * @param advisor the AI feature that explains spending
     * @param reports writes the statement file
     */
    public StudentMenu(Scanner sc, StudentAccount user, AccountManager am, TransactionManager tm,
                       SpendingAdvisor advisor, ReportGenerator reports) {
        this.sc = sc;
        this.user = user;
        this.am = am;
        this.tm = tm;
        this.advisor = advisor;
        this.reports = reports;
    }

    /** Shows the student menu until the student logs out. */
    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- STUDENT MENU (" + user.getAccountId() + ", " + user.getName() + ") ---");
            System.out.println("1. View balance");
            System.out.println("2. Top up");
            System.out.println("3. Pay a merchant");
            System.out.println("4. Transfer to a student");
            System.out.println("5. Transaction history");
            System.out.println("6. Explain my spending (AI)");
            System.out.println("7. Export my statement");
            System.out.println("0. Log out");
            int choice = InputValidator.readInt(sc, "Choose: ", 0, 7);
            switch (choice) {
                case 1: viewBalance(); break;
                case 2: doTopUp(); break;
                case 3: doPay(); break;
                case 4: doTransfer(); break;
                case 5: viewHistory(); break;
                case 6: explainSpending(); break;
                case 7: exportStatement(); break;
                case 0: back = true; break;
            }
        }
        System.out.println("Logged out.");
    }

    /** Shows the balance, the daily limit, what is left of it today, and a frozen warning. */
    private void viewBalance() {
        System.out.printf("Balance: RM %.2f%n", user.getBalance());
        System.out.printf("Daily limit: RM %.2f (left today: RM %.2f)%n",
                user.getDailyLimit(), user.getRemainingLimit());
        if (user.isFrozen()) {
            System.out.println("Status: FROZEN (payments and transfers are blocked)");
        }
    }

    /** Adds money to the student's balance. */
    private void doTopUp() {
        double amount = InputValidator.readDouble(sc, "Top-up amount (RM): ", 1.00, MAX_TOP_UP);
        try {
            user.topUp(amount);
            record("TOPUP", user.getAccountId(), amount, 0.0, "SUCCESS");
            System.out.printf("Top-up done. New balance: RM %.2f%n", user.getBalance());
        } catch (IllegalArgumentException e) {
            System.out.println("Top-up failed: " + e.getMessage());
        }
    }

    /** Pays a merchant after the student confirms. A failed payment is recorded as FAILED. */
    private void doPay() {
        String id = InputValidator.readNonEmpty(sc, "Merchant ID (e.g. M0001): ").toUpperCase();
        Account found = am.findById(id);
        if (!(found instanceof MerchantAccount)) {
            System.out.println("No merchant with ID " + id + ".");
            return;
        }
        MerchantAccount shop = (MerchantAccount) found;
        double amount = InputValidator.readDouble(sc, "Amount to pay (RM): ", 0.01, MAX_AMOUNT);
        System.out.printf("Pay RM %.2f to %s?%n", amount, shop.getBusinessName());
        if (!InputValidator.readYesNo(sc, "Confirm (y/n): ")) {
            System.out.println("Payment cancelled.");
            return;
        }
        try {
            user.pay(shop, amount);
            // the fee is the merchant's 1.5% receiving fee; the student pays only the amount
            record("PAYMENT", shop.getAccountId(), amount, shop.calculateFee(amount), "SUCCESS");
            System.out.printf("Payment done. New balance: RM %.2f%n", user.getBalance());
        } catch (InsufficientFundsException e) {
            failed("PAYMENT", shop.getAccountId(), amount, "Payment failed. " + e.getMessage());
        } catch (DailyLimitExceededException e) {
            failed("PAYMENT", shop.getAccountId(), amount, "Payment failed. " + e.getMessage());
        } catch (AccountFrozenException e) {
            failed("PAYMENT", shop.getAccountId(), amount, "Payment failed. " + e.getMessage());
        }
    }

    /** Sends money to another student after the student confirms. A failed transfer is recorded as FAILED. */
    private void doTransfer() {
        String id = InputValidator.readMatricNumber(sc, "Receiver's matric number: ");
        if (id.equals(user.getAccountId())) {
            System.out.println("You cannot transfer to yourself.");
            return;
        }
        Account found = am.findById(id);
        if (!(found instanceof StudentAccount)) {
            System.out.println("No student with matric number " + id + ".");
            return;
        }
        StudentAccount receiver = (StudentAccount) found;
        double amount = InputValidator.readDouble(sc, "Amount to transfer (RM): ", 0.01, MAX_AMOUNT);
        double fee = StudentAccount.TRANSFER_FEE;
        System.out.printf("Transfer RM %.2f to %s? The fee is RM %.2f, so RM %.2f leaves your balance.%n",
                amount, receiver.getName(), fee, amount + fee);
        if (!InputValidator.readYesNo(sc, "Confirm (y/n): ")) {
            System.out.println("Transfer cancelled.");
            return;
        }
        try {
            user.transfer(receiver, amount);
            record("TRANSFER", receiver.getAccountId(), amount, fee, "SUCCESS");
            System.out.printf("Transfer done. New balance: RM %.2f%n", user.getBalance());
        } catch (InsufficientFundsException e) {
            failed("TRANSFER", receiver.getAccountId(), amount, "Transfer failed. " + e.getMessage());
        } catch (DailyLimitExceededException e) {
            failed("TRANSFER", receiver.getAccountId(), amount, "Transfer failed. " + e.getMessage());
        } catch (AccountFrozenException e) {
            failed("TRANSFER", receiver.getAccountId(), amount, "Transfer failed. " + e.getMessage());
        }
    }

    /** Lists every transaction where this student sent or received money. */
    private void viewHistory() {
        Transaction[] list = tm.findByAccount(user.getAccountId());
        if (list.length == 0) {
            System.out.println("No transactions yet.");
            return;
        }
        System.out.println("Your transactions:");
        for (int i = 0; i < list.length; i++) {
            System.out.println((i + 1) + ". " + list[i]);
        }
    }

    /** Asks the AI to explain the student's spending; shows a fallback if the AI is unavailable. */
    private void explainSpending() {
        Transaction[] list = tm.findByAccount(user.getAccountId());
        if (list.length == 0) {
            System.out.println("You have no transactions yet, so there is nothing to explain.");
            return;
        }
        System.out.println("Asking the AI, please wait...");
        try {
            String reply = advisor.explainSpending(user, list);
            System.out.println("\n" + reply);
        } catch (AIServiceException e) {
            System.out.println("The AI is not available right now: " + e.getMessage());
            System.out.println("Everything else in the e-wallet still works.");
        }
    }

    /** Saves this student's statement to reports/statement_<id>.txt. */
    private void exportStatement() {
        String path = reports.generateStatement(user, tm);
        if (path != null) {
            System.out.println("Statement saved to " + path);
        } else {
            System.out.println("The statement could not be saved.");
        }
    }

    /** Prints why an action failed and keeps a FAILED record of it (no money moved, so no fee). */
    private void failed(String type, String to, double amount, String message) {
        System.out.println(message);
        record(type, to, amount, 0.0, "FAILED");
    }

    /** Records one action by this student in the transaction list. For a top-up, "to" is the student. */
    private void record(String type, String to, double amount, double fee, String status) {
        String time = LocalDateTime.now().format(TIME_FORMAT);
        double roundedFee = Math.round(fee * 100) / 100.0;
        Transaction t = new Transaction(tm.generateNewId(), type, user.getAccountId(), to,
                amount, roundedFee, time, status);
        if (!tm.addTransaction(t)) {
            System.out.println("Warning: the transaction list is full, so this record was not kept.");
        }
    }
}
