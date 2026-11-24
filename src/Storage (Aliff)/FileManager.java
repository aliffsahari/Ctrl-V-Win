import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Saves all application data to .txt files and loads it back on startup.
 *
 * File formats (one record per line, fields separated by '|', no header):
 *   data/accounts.txt
 *     STUDENT|S0001|Name|1234|150.50|false|500.00
 *     MERCHANT|M0001|Name|4321|980.00|false|Cafe Name
 *     fields: type, id, name, pin, balance, frozen, dailyLimit OR businessName
 *   data/transactions.txt
 *     T00001|PAYMENT|S0001|M0001|12.50|0.19|2026-12-01 10:30|SUCCESS
 *     fields: id, type, senderId, receiverId, amount, fee, dateTime, status
 *
 * Lines that are empty or start with '#' are ignored. A bad line is skipped
 * and reported, so one corrupt line never stops the whole load.
 * Names and business names must not contain the '|' character
 * (InputValidator should reject it).
 *
 * Owner: Data & Storage Developer.
 */
public class FileManager {

    private static final String DATA_FOLDER = "data/";
    private static final String ACCOUNTS_FILE = "data/accounts.txt";
    private static final String TRANSACTIONS_FILE = "data/transactions.txt";
    private static final String DELIMITER_REGEX = "\\|";

    /**
     * Saves every account and transaction to the .txt files.
     * Each file is written to a temporary file first and then moved over
     * the real one, so a crash half way through cannot leave a half-written
     * data file. Problems are reported on screen; the program keeps running.
     * @param am the accounts to save
     * @param tm the transactions to save
     */
    public void saveAll(AccountManager am, TransactionManager tm) {
        ensureDataFolder();
        try {
            File tmp = new File(ACCOUNTS_FILE + ".tmp");
            try (PrintWriter pw = new PrintWriter(new FileWriter(tmp, StandardCharsets.UTF_8))) {
                for (int i = 0; i < am.getCount(); i++) {
                    pw.println(am.getAt(i).toFileString());
                }
                if (pw.checkError()) {
                    throw new IOException("could not write " + ACCOUNTS_FILE);
                }
            }
            Files.move(tmp.toPath(), new File(ACCOUNTS_FILE).toPath(),
                       StandardCopyOption.REPLACE_EXISTING);

            tmp = new File(TRANSACTIONS_FILE + ".tmp");
            try (PrintWriter pw = new PrintWriter(new FileWriter(tmp, StandardCharsets.UTF_8))) {
                for (int i = 0; i < tm.getCount(); i++) {
                    pw.println(tm.getAt(i).toFileString());
                }
                if (pw.checkError()) {
                    throw new IOException("could not write " + TRANSACTIONS_FILE);
                }
            }
            Files.move(tmp.toPath(), new File(TRANSACTIONS_FILE).toPath(),
                       StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Warning: data could not be saved (" + e.getMessage() + ").");
        }
    }

    /**
     * Loads accounts and transactions from the .txt files into the managers.
     * The managers are emptied first. Missing files or folders are created
     * (empty) instead of crashing, and bad lines are skipped with a message.
     * @param am the account manager to fill
     * @param tm the transaction manager to fill
     */
    public void loadAll(AccountManager am, TransactionManager tm) {
        ensureDataFolder();
        am.clear();
        tm.clear();
        loadAccounts(am);
        loadTransactions(tm);
        System.out.println("Loaded " + am.getCount() + " account(s) and "
                           + tm.getCount() + " transaction(s).");
    }

    /** Reads accounts.txt line by line. */
    private void loadAccounts(AccountManager am) {
        File file = new File(ACCOUNTS_FILE);
        if (!createIfMissing(file)) {
            return;
        }
        int lineNumber = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                Account a = parseAccount(line);
                if (a == null || !am.addAccount(a)) {
                    System.out.println("Skipped bad line " + lineNumber + " in " + ACCOUNTS_FILE);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read " + ACCOUNTS_FILE + " (" + e.getMessage() + ").");
        }
    }

    /** Reads transactions.txt line by line. */
    private void loadTransactions(TransactionManager tm) {
        File file = new File(TRANSACTIONS_FILE);
        if (!createIfMissing(file)) {
            return;
        }
        int lineNumber = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                Transaction t = parseTransaction(line);
                if (t == null || !tm.addTransaction(t)) {
                    System.out.println("Skipped bad line " + lineNumber + " in " + TRANSACTIONS_FILE);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read " + TRANSACTIONS_FILE + " (" + e.getMessage() + ").");
        }
    }

    /**
     * Turns one accounts.txt line into the right Account subclass.
     * @param line a line such as STUDENT|S0001|Ali|1234|150.50|false|500.00
     * @return the account, or null if the line is not valid
     */
    private Account parseAccount(String line) {
        String[] p = line.split(DELIMITER_REGEX, -1);
        if (p.length < 7) {
            return null;
        }
        String type = p[0].trim().toUpperCase();
        String id = p[1].trim();
        String name = p[2].trim();
        String pin = p[3].trim();
        String extra = p[6].trim();
        if (id.isEmpty() || name.isEmpty() || pin.isEmpty()) {
            return null;
        }
        try {
            double balance = Double.parseDouble(p[4].trim());
            boolean frozen = Boolean.parseBoolean(p[5].trim());
            if (type.equals("STUDENT")) {
                double dailyLimit = Double.parseDouble(extra);
                return new StudentAccount(id, name, pin, balance, frozen, dailyLimit);
            } else if (type.equals("MERCHANT")) {
                if (extra.isEmpty()) {
                    return null;
                }
                return new MerchantAccount(id, name, pin, balance, frozen, extra);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null; // unknown account type
    }

    /**
     * Turns one transactions.txt line into a Transaction.
     * @param line a line such as T00001|PAYMENT|S0001|M0001|12.50|0.19|2026-12-01 10:30|SUCCESS
     * @return the transaction, or null if the line is not valid
     */
    private Transaction parseTransaction(String line) {
        String[] p = line.split(DELIMITER_REGEX, -1);
        if (p.length < 8) {
            return null;
        }
        String id = p[0].trim();
        if (id.isEmpty()) {
            return null;
        }
        try {
            double amount = Double.parseDouble(p[4].trim());
            double fee = Double.parseDouble(p[5].trim());
            return new Transaction(id, p[1].trim(), p[2].trim(), p[3].trim(),
                                   amount, fee, p[6].trim(), p[7].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Creates the data folder if it does not exist yet. */
    private void ensureDataFolder() {
        File folder = new File(DATA_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * Makes sure a data file exists. A first run has no files yet.
     * @return true if the file exists (or was just created)
     */
    private boolean createIfMissing(File file) {
        if (file.exists()) {
            return true;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            System.out.println("Warning: could not create " + file.getPath());
            return false;
        }
    }
}
