import java.util.Scanner;

/**
 * The admin menu: list, create, freeze, unfreeze and delete accounts,
 * and save the admin report.
 *
 * Owner: Rinn (Interface and Quality Developer), Group 6.
 */
public class AdminMenu {

    private static final double MAX_OPENING_BALANCE = 5000.00;

    private Scanner sc;
    private AccountManager am;
    private TransactionManager tm;
    private ReportGenerator reports;

    /**
     * Creates the admin menu.
     * @param sc the Scanner for keyboard input
     * @param am all accounts
     * @param tm all transactions, used by the report
     * @param reports writes the admin report file
     */
    public AdminMenu(Scanner sc, AccountManager am, TransactionManager tm, ReportGenerator reports) {
        this.sc = sc;
        this.am = am;
        this.tm = tm;
        this.reports = reports;
    }

    /** Shows the admin menu until the admin logs out. */
    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. List all accounts");
            System.out.println("2. Create an account");
            System.out.println("3. Freeze an account");
            System.out.println("4. Unfreeze an account");
            System.out.println("5. Delete an account");
            System.out.println("6. Generate admin report");
            System.out.println("0. Log out");
            int choice = InputValidator.readInt(sc, "Choose: ", 0, 6);
            switch (choice) {
                case 1: listAccounts(); break;
                case 2: createAccount(); break;
                case 3: freezeAccount(); break;
                case 4: unfreezeAccount(); break;
                case 5: deleteAccount(); break;
                case 6: generateAdminReport(); break;
                case 0: back = true; break;
            }
        }
        System.out.println("Logged out.");
    }

    /** Prints every account, one per line. */
    private void listAccounts() {
        if (am.getCount() == 0) {
            System.out.println("No accounts yet.");
            return;
        }
        for (int i = 0; i < am.getCount(); i++) {
            System.out.println((i + 1) + ". " + am.getAt(i));
        }
    }

    /** Creates a student (ID = matric number) or merchant (ID = next M number) with an opening balance. */
    private void createAccount() {
        if (am.isFull()) {
            System.out.println("No new accounts can be added (the account list is full).");
            return;
        }
        System.out.println("1. Student");
        System.out.println("2. Merchant");
        int type = InputValidator.readInt(sc, "Account type: ", 1, 2);

        String matric = null;
        if (type == 1) {
            matric = InputValidator.readMatricNumber(sc, "Matric number (" + InputValidator.MATRIC_LENGTH + " digits): ");
            if (am.idExists(matric)) {
                System.out.println("An account with matric number " + matric + " already exists.");
                return;
            }
        }
        String name = InputValidator.readNonEmpty(sc, "Full name: ");
        String pin = InputValidator.readNewPin(sc);
        double balance = InputValidator.readDouble(sc, "Opening balance (RM): ", 0.00, MAX_OPENING_BALANCE);

        Account a;
        if (type == 1) {
            String normal = String.format("%.2f", StudentAccount.DEFAULT_DAILY_LIMIT);
            double limit = InputValidator.readDouble(sc, "Daily limit (RM, normally " + normal + "): ",
                    10.00, 5000.00);
            a = new StudentAccount(matric, name, pin, balance, false, limit);
        } else {
            String business = InputValidator.readNonEmpty(sc, "Business name: ");
            a = new MerchantAccount(am.generateNewId("M"), name, pin, balance, false, business);
        }

        if (am.addAccount(a)) {
            System.out.println("Created: " + a);
        } else {
            System.out.println("The account could not be created.");
        }
    }

    /** Freezes an account so it cannot pay, transfer or refund. */
    private void freezeAccount() {
        Account a = askForAccount();
        if (a == null) {
            return;
        }
        if (a.isFrozen()) {
            System.out.println(a.getAccountId() + " is already frozen.");
        } else {
            a.freeze();
            System.out.println(a.getAccountId() + " is now frozen.");
        }
    }

    /** Unfreezes a frozen account. */
    private void unfreezeAccount() {
        Account a = askForAccount();
        if (a == null) {
            return;
        }
        if (!a.isFrozen()) {
            System.out.println(a.getAccountId() + " is not frozen.");
        } else {
            a.unfreeze();
            System.out.println(a.getAccountId() + " is active again.");
        }
    }

    /** Deletes an account after the admin confirms. */
    private void deleteAccount() {
        Account a = askForAccount();
        if (a == null) {
            return;
        }
        System.out.println(a);
        if (a.getBalance() > 0) {
            System.out.printf("Warning: this account still has RM %.2f.%n", a.getBalance());
        }
        if (!InputValidator.readYesNo(sc, "Delete this account for good? (y/n): ")) {
            System.out.println("Nothing was deleted.");
            return;
        }
        if (am.deleteAccount(a.getAccountId())) {
            System.out.println(a.getAccountId() + " was deleted.");
        } else {
            System.out.println("The account could not be deleted.");
        }
    }

    /** Saves the admin report to reports/admin_report.txt. */
    private void generateAdminReport() {
        String path = reports.generateAdminReport(am, tm);
        if (path != null) {
            System.out.println("Report saved to " + path);
        } else {
            System.out.println("The report could not be saved.");
        }
    }

    /** Asks for an account ID and returns that account, or null if it does not exist. */
    private Account askForAccount() {
        String id = InputValidator.readNonEmpty(sc, "Account ID: ").toUpperCase();
        Account a = am.findById(id);
        if (a == null) {
            System.out.println("No account with ID " + id + ".");
        }
        return a;
    }
}
