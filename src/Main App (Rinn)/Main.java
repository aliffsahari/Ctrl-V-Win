import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Entry point of the student e-wallet. Loads the saved data, shows the
 * login menu, opens the right menu for each user, and saves everything on exit.
 *
 * Owner: Rinn (Interface and Quality Developer), Group 6.
 */
public class Main {

    /** Demo admin login. A real system would keep a hashed password in a file. */
    private static final String ADMIN_ID = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    /** Wrong PINs allowed before going back to the main menu. */
    private static final int MAX_PIN_TRIES = 3;

    /** No objects are made from this class: it only holds the static main method and its helpers. */
    private Main() {
    }

    /**
     * Loads the data, runs the login loop until the user exits, then saves.
     * @param args not used
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AccountManager am = new AccountManager();
        TransactionManager tm = new TransactionManager();
        FileManager fm = new FileManager();
        fm.loadAll(am, tm);

        SpendingAdvisor advisor = new SpendingAdvisor(new AIHelper());
        ReportGenerator reports = new ReportGenerator();
        Admin admin = new Admin(ADMIN_ID, ADMIN_PASSWORD);

        try {
            showLoginMenu(sc, am, tm, fm, advisor, reports, admin);
        } catch (NoSuchElementException e) {
            // the input was closed (for example Ctrl+D): save below instead of crashing
            System.out.println("\nInput closed.");
        }

        fm.saveAll(am, tm);
        System.out.println("Data saved. Goodbye!");
    }

    /** The first menu: log in, register, admin log in, or exit. */
    private static void showLoginMenu(Scanner sc, AccountManager am, TransactionManager tm, FileManager fm,
                                      SpendingAdvisor advisor, ReportGenerator reports, Admin admin) {
        boolean running = true;
        while (running) {
            System.out.println("\n===== STUDENT E-WALLET =====");
            System.out.println("1. Log in");
            System.out.println("2. Register a new account");
            System.out.println("3. Admin log in");
            System.out.println("0. Exit");
            int choice = InputValidator.readInt(sc, "Choose: ", 0, 3);
            switch (choice) {
                case 1:
                    Account user = login(sc, am);
                    if (user instanceof StudentAccount) {
                        new StudentMenu(sc, (StudentAccount) user, am, tm, advisor, reports).show();
                    } else if (user instanceof MerchantAccount) {
                        new MerchantMenu(sc, (MerchantAccount) user, am, tm).show();
                    }
                    if (user != null) {
                        fm.saveAll(am, tm);          // save after every session
                    }
                    break;
                case 2:
                    if (register(sc, am)) {
                        fm.saveAll(am, tm);
                    }
                    break;
                case 3:
                    if (adminLogin(sc, admin)) {
                        new AdminMenu(sc, am, tm, reports).show();
                        fm.saveAll(am, tm);
                    }
                    break;
                case 0:
                    running = false;
                    break;
            }
        }
    }

    /**
     * Asks for an account ID and PIN.
     * @return the account, or null if the ID is unknown or the PIN is wrong 3 times
     */
    private static Account login(Scanner sc, AccountManager am) {
        String id = InputValidator.readNonEmpty(sc, "Account ID (student: matric number, merchant: e.g. M0001): ")
                .toUpperCase();
        Account a = am.findById(id);
        if (a == null) {
            System.out.println("No account with ID " + id + ".");
            return null;
        }
        for (int tries = 1; tries <= MAX_PIN_TRIES; tries++) {
            String pin = InputValidator.readPin(sc, "PIN: ");
            if (a.checkPin(pin)) {
                System.out.println("Welcome, " + a.getName() + "!");
                if (a.isFrozen()) {
                    System.out.println("Note: this account is frozen, so it cannot send money.");
                }
                return a;
            }
            int left = MAX_PIN_TRIES - tries;
            if (left > 0) {
                System.out.println("Wrong PIN. Tries left: " + left);
            }
        }
        System.out.println("Wrong PIN " + MAX_PIN_TRIES + " times. Back to the main menu.");
        return null;
    }

    /**
     * Creates a new student or merchant account with a zero balance.
     * Students use their matric number as the account ID; merchants get the next free M number.
     * @return true if an account was created
     */
    private static boolean register(Scanner sc, AccountManager am) {
        System.out.println("\n--- REGISTER ---");
        if (am.isFull()) {
            System.out.println("Sorry, no new accounts can be added (the account list is full).");
            return false;
        }
        System.out.println("1. Student");
        System.out.println("2. Merchant");
        int type = InputValidator.readInt(sc, "Account type: ", 1, 2);

        String matric = null;
        if (type == 1) {
            matric = InputValidator.readMatricNumber(sc, "Matric number (" + InputValidator.MATRIC_LENGTH + " digits): ");
            if (am.idExists(matric)) {
                System.out.println("An account with matric number " + matric + " already exists. Please log in.");
                return false;
            }
        }
        String name = InputValidator.readNonEmpty(sc, "Full name: ");
        String pin = InputValidator.readNewPin(sc);

        Account a;
        if (type == 1) {
            a = new StudentAccount(matric, name, pin, 0.0, false, StudentAccount.DEFAULT_DAILY_LIMIT);
        } else {
            String business = InputValidator.readNonEmpty(sc, "Business name: ");
            a = new MerchantAccount(am.generateNewId("M"), name, pin, 0.0, false, business);
        }

        if (am.addAccount(a)) {
            System.out.println("Account created. Your account ID is " + a.getAccountId() + ". Use it to log in.");
            return true;
        }
        System.out.println("Sorry, the account could not be created.");
        return false;
    }

    /** Checks the admin ID and password. */
    private static boolean adminLogin(Scanner sc, Admin admin) {
        String id = InputValidator.readNonEmpty(sc, "Admin ID: ");
        String password = InputValidator.readNonEmpty(sc, "Password: ");
        if (id.equals(admin.getAdminId()) && admin.checkPassword(password)) {
            return true;
        }
        System.out.println("Wrong admin ID or password.");
        return false;
    }
}
