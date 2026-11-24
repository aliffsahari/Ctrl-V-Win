import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Creates the text reports required by the assignment and saves them
 * as .txt files in the reports/ folder:
 *   reports/statement_<accountId>.txt  - one account's statement
 *   reports/admin_report.txt           - overview of the whole system
 * Only plain arrays and loops are used.
 *
 * Owner: Data & Storage Developer.
 */
public class ReportGenerator {

    private static final String REPORT_FOLDER = "reports/";
    private static final String[] TYPES = {"TOPUP", "PAYMENT", "TRANSFER", "REFUND"};
    private static final String LINE = "------------------------------------------------------------------------------";
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Writes a statement for one account listing all its transactions.
     * @param a the account
     * @param tm the transactions to search
     * @return the path of the file written, or null if it could not be written
     */
    public String generateStatement(Account a, TransactionManager tm) {
        if (a == null) {
            return null;
        }
        ensureReportFolder();
        String path = REPORT_FOLDER + "statement_" + a.getAccountId() + ".txt";
        Transaction[] history = tm.findByAccount(a.getAccountId());

        try (PrintWriter pw = new PrintWriter(new FileWriter(path, StandardCharsets.UTF_8))) {
            pw.println("ACCOUNT STATEMENT");
            pw.println(LINE);
            pw.println("Generated   : " + LocalDateTime.now().format(STAMP));
            pw.println("Account ID  : " + a.getAccountId());
            pw.println("Name        : " + a.getName());
            pw.println("Type        : " + a.getAccountType());
            pw.println("Status      : " + (a.isFrozen() ? "FROZEN" : "ACTIVE"));
            pw.println("Balance     : RM " + money(a.getBalance()));
            pw.println(LINE);
            pw.println(String.format(Locale.US, "%-16s %-7s %-9s %-4s %-8s %10s %7s %-7s",
                    "Date", "ID", "Type", "Dir", "Other", "Amount", "Fee", "Status"));
            pw.println(LINE);

            if (history.length == 0) {
                pw.println("(no transactions yet)");
            }
            for (int i = 0; i < history.length; i++) {
                pw.println(formatLine(history[i], a.getAccountId()));
            }

            pw.println(LINE);
            pw.println("Transactions listed : " + history.length);
            pw.println("Total paid out      : RM " + money(tm.totalSpent(a.getAccountId())));
            pw.println("Total received      : RM " + money(tm.totalReceived(a.getAccountId())));
            pw.println(LINE);
            pw.println("End of statement");
            if (pw.checkError()) {
                throw new IOException("write failed");
            }
            return path;
        } catch (IOException e) {
            System.out.println("Warning: could not write " + path + " (" + e.getMessage() + ").");
            return null;
        }
    }

    /**
     * Writes an overview report for the admin: account totals, money held,
     * transactions per type, fees collected, the top spender and a list of
     * all accounts.
     * @param am all accounts
     * @param tm all transactions
     * @return the path of the file written, or null if it could not be written
     */
    public String generateAdminReport(AccountManager am, TransactionManager tm) {
        ensureReportFolder();
        String path = REPORT_FOLDER + "admin_report.txt";

        // Totals over all accounts
        int frozenCount = 0;
        double totalBalance = 0;
        String topId = null;
        double topSpent = 0;
        for (int i = 0; i < am.getCount(); i++) {
            Account a = am.getAt(i);
            totalBalance += a.getBalance();
            if (a.isFrozen()) {
                frozenCount++;
            }
            double spent = tm.totalSpent(a.getAccountId());
            if (spent > topSpent) {
                topSpent = spent;
                topId = a.getAccountId();
            }
        }

        // Totals over all transactions, one slot per type
        int[] typeCount = new int[TYPES.length];
        double[] typeAmount = new double[TYPES.length];
        int failed = 0;
        double feesCollected = 0;
        for (int i = 0; i < tm.getCount(); i++) {
            Transaction t = tm.getAt(i);
            if (!"SUCCESS".equalsIgnoreCase(t.getStatus())) {
                failed++;
                continue;
            }
            feesCollected += t.getFee();
            for (int k = 0; k < TYPES.length; k++) {
                if (TYPES[k].equalsIgnoreCase(t.getType())) {
                    typeCount[k]++;
                    typeAmount[k] += t.getAmount();
                }
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(path, StandardCharsets.UTF_8))) {
            pw.println("ADMIN REPORT");
            pw.println(LINE);
            pw.println("Generated           : " + LocalDateTime.now().format(STAMP));
            pw.println("Total accounts      : " + am.getCount()
                    + " (students: " + am.countByType("STUDENT")
                    + ", merchants: " + am.countByType("MERCHANT") + ")");
            pw.println("Frozen accounts     : " + frozenCount);
            pw.println("Money in all wallets: RM " + money(totalBalance));
            pw.println("Total transactions  : " + tm.getCount() + " (failed/other: " + failed + ")");
            pw.println("Fees collected      : RM " + money(feesCollected));
            if (topId == null) {
                pw.println("Top spender         : (none yet)");
            } else {
                pw.println("Top spender         : " + topId + " (RM " + money(topSpent) + ")");
            }
            pw.println(LINE);
            pw.println("Successful transactions by type");
            for (int k = 0; k < TYPES.length; k++) {
                pw.println(String.format(Locale.US, "  %-9s count: %-5d total: RM %s",
                        TYPES[k], typeCount[k], money(typeAmount[k])));
            }
            pw.println(LINE);
            pw.println(String.format(Locale.US, "%-8s %-22s %-9s %12s %-7s",
                    "ID", "Name", "Type", "Balance", "Status"));
            pw.println(LINE);
            if (am.getCount() == 0) {
                pw.println("(no accounts)");
            }
            for (int i = 0; i < am.getCount(); i++) {
                Account a = am.getAt(i);
                pw.println(String.format(Locale.US, "%-8s %-22s %-9s %12s %-7s",
                        a.getAccountId(), shorten(a.getName(), 22), a.getAccountType(),
                        money(a.getBalance()), a.isFrozen() ? "FROZEN" : "ACTIVE"));
            }
            pw.println(LINE);
            pw.println("End of report");
            if (pw.checkError()) {
                throw new IOException("write failed");
            }
            return path;
        } catch (IOException e) {
            System.out.println("Warning: could not write " + path + " (" + e.getMessage() + ").");
            return null;
        }
    }

    /**
     * Formats one transaction as a table row from one account's point of view.
     * @param t the transaction
     * @param viewerId the account the statement is for (decides IN or OUT)
     * @return a single formatted line
     */
    private String formatLine(Transaction t, String viewerId) {
        boolean topUp = t.getType().equalsIgnoreCase("TOPUP");
        boolean outgoing = !topUp && viewerId.equalsIgnoreCase(t.getSenderId());
        String direction = outgoing ? "OUT" : "IN";
        String other = topUp ? "-" : (outgoing ? t.getReceiverId() : t.getSenderId());
        return String.format(Locale.US, "%-16s %-7s %-9s %-4s %-8s %10s %7s %-7s",
                t.getDateTime(), t.getTransactionId(), t.getType(), direction,
                other, money(t.getAmount()), money(t.getFee()), t.getStatus());
    }

    /** Formats an amount with two decimals and a dot, whatever the PC's locale. */
    private String money(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    /** Cuts long text so table columns stay lined up. */
    private String shorten(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 1) + ".";
    }

    /** Creates the reports folder if it does not exist yet. */
    private void ensureReportFolder() {
        File folder = new File(REPORT_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}
