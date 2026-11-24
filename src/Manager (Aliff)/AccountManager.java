import java.util.Locale;

/**
 * Stores all accounts in a plain Account[] array with a separate counter.
 * No ArrayList / HashMap is used: searching, adding and deleting are done
 * with loops, as the assignment requires.
 *
 * Owner: Data & Storage Developer.
 */
public class AccountManager {

    /** Maximum number of accounts the array can hold. */
    public static final int MAX_ACCOUNTS = 200;

    private Account[] accounts;
    private int accountCount;

    /** Creates an empty manager. */
    public AccountManager() {
        accounts = new Account[MAX_ACCOUNTS];
        accountCount = 0;
    }

    /**
     * Adds an account at the end of the array.
     * @param a the account to add
     * @return false if the account is null, the array is full,
     *         or an account with the same ID already exists
     */
    public boolean addAccount(Account a) {
        if (a == null || accountCount >= MAX_ACCOUNTS) {
            return false;
        }
        if (idExists(a.getAccountId())) {
            return false;
        }
        accounts[accountCount] = a;
        accountCount++;
        return true;
    }

    /**
     * Finds an account by its ID (case-insensitive) using a loop search.
     * @param id the account ID to look for
     * @return the account, or null if not found
     */
    public Account findById(String id) {
        int index = findIndexById(id);
        if (index == -1) {
            return null;
        }
        return accounts[index];
    }

    /**
     * Loop search that returns the array position of an ID.
     * @param id the account ID to look for
     * @return the index, or -1 if not found
     */
    private int findIndexById(String id) {
        if (id == null) {
            return -1;
        }
        String target = id.trim();
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getAccountId().equalsIgnoreCase(target)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Deletes an account by shifting every later element one place to the
     * left, clearing the last slot and lowering the counter.
     * @param id the ID of the account to remove
     * @return true if it was found and removed
     */
    public boolean deleteAccount(String id) {
        int index = findIndexById(id);
        if (index == -1) {
            return false;
        }
        for (int i = index; i < accountCount - 1; i++) {
            accounts[i] = accounts[i + 1];
        }
        accounts[accountCount - 1] = null;
        accountCount--;
        return true;
    }

    /**
     * Checks whether an ID is already used.
     * @param id the ID to check
     * @return true if an account with this ID exists
     */
    public boolean idExists(String id) {
        return findIndexById(id) != -1;
    }

    /** @return how many accounts are stored */
    public int getCount() {
        return accountCount;
    }

    /** @return true when no more accounts can be added */
    public boolean isFull() {
        return accountCount >= MAX_ACCOUNTS;
    }

    /**
     * Returns the account at a position, for loops in other classes.
     * @param index position from 0 to getCount() - 1
     * @return the account, or null if the index is out of range
     */
    public Account getAt(int index) {
        if (index < 0 || index >= accountCount) {
            return null;
        }
        return accounts[index];
    }

    /**
     * Counts accounts of one type.
     * @param type "STUDENT" or "MERCHANT"
     * @return how many accounts have this type
     */
    public int countByType(String type) {
        int count = 0;
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getAccountType().equalsIgnoreCase(type)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Creates the next free ID for a prefix, e.g. "S" gives S0001, S0002...
     * It looks at the highest number already used, so IDs of deleted
     * accounts are not reused by accident.
     * @param prefix the letter(s) at the start of the ID
     * @return a new unused ID
     */
    public String generateNewId(String prefix) {
        int max = 0;
        for (int i = 0; i < accountCount; i++) {
            String id = accounts[i].getAccountId();
            if (id.toUpperCase().startsWith(prefix.toUpperCase())) {
                try {
                    int number = Integer.parseInt(id.substring(prefix.length()));
                    if (number > max) {
                        max = number;
                    }
                } catch (NumberFormatException e) {
                    // ID does not end in a number: ignore it
                }
            }
        }
        return prefix + String.format(Locale.US, "%04d", max + 1);
    }

    /** Removes every account (used by FileManager before loading). */
    public void clear() {
        for (int i = 0; i < accountCount; i++) {
            accounts[i] = null;
        }
        accountCount = 0;
    }
}
