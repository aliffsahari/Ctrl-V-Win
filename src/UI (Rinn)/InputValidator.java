import java.util.Scanner;

/**
 * Reads and checks every keyboard input for the e-wallet menus.
 * Each method keeps asking until the input is valid, so bad input never
 * crashes the program. All methods are static, for example:
 * InputValidator.readInt(sc, "Choose: ", 0, 5)
 *
 * Owner: Rinn (Interface & Quality Developer), Group 6.
 */
public final class InputValidator {

    /** Every PIN has exactly this many digits, for example 1234. */
    public static final int PIN_LENGTH = 4;

    /** A matric number (the student's account ID) has exactly this many digits, for example 25000001. */
    public static final int MATRIC_LENGTH = 8;

    /** No objects are made from this class: it only holds static helpers. */
    private InputValidator() {
    }

    /**
     * Reads a whole number from min to max.
     * @param sc the Scanner for keyboard input
     * @param prompt the text shown before the input
     * @param min the smallest allowed value
     * @param max the largest allowed value
     * @return a whole number from min to max
     */
    public static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // not a whole number: show the message below and ask again
            }
            System.out.println("Please enter a whole number from " + min + " to " + max + ".");
        }
    }

    /**
     * Reads an amount of money from min to max, with at most 2 decimal places.
     * @param sc the Scanner for keyboard input
     * @param prompt the text shown before the input
     * @param min the smallest allowed amount
     * @param max the largest allowed amount
     * @return a valid amount
     */
    public static double readDouble(Scanner sc, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                boolean twoDecimals = Math.round(value * 100) / 100.0 == value;
                if (value >= min && value <= max && twoDecimals) {
                    return value + 0.0;   // + 0.0 turns -0 into 0, so it never prints -0.00
                }
            } catch (NumberFormatException e) {
                // not a number: show the message below and ask again
            }
            System.out.printf("Please enter an amount from %.2f to %.2f, with at most 2 decimals.%n", min, max);
        }
    }

    /**
     * Reads a line of text that is not empty and has no '|'
     * (the '|' character separates the fields in our .txt files).
     * @param sc the Scanner for keyboard input
     * @param prompt the text shown before the input
     * @return the text, with spaces removed from both ends
     */
    public static String readNonEmpty(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("This cannot be empty.");
            } else if (line.indexOf('|') >= 0) {
                System.out.println("Please do not use the | character.");
            } else {
                return line;
            }
        }
    }

    /**
     * Reads a yes or no answer: y, yes, n or no (capital letters also work).
     * @param sc the Scanner for keyboard input
     * @param prompt the text shown before the input
     * @return true for yes, false for no
     */
    public static boolean readYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            System.out.println("Please type y or n.");
        }
    }

    /**
     * Reads a PIN: exactly PIN_LENGTH digits.
     * @param sc the Scanner for keyboard input
     * @param prompt the text shown before the input
     * @return the PIN as text, so a PIN like 0123 keeps its first 0
     */
    public static String readPin(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (isDigits(line) && line.length() == PIN_LENGTH) {
                return line;
            }
            System.out.println("A PIN has exactly " + PIN_LENGTH + " digits.");
        }
    }

    /**
     * Reads a new PIN twice and returns it once both entries match.
     * Used when an account is registered or created.
     * @param sc the Scanner for keyboard input
     * @return the new PIN
     */
    public static String readNewPin(Scanner sc) {
        while (true) {
            String pin = readPin(sc, "Choose a " + PIN_LENGTH + "-digit PIN: ");
            String again = readPin(sc, "Type the PIN again: ");
            if (pin.equals(again)) {
                return pin;
            }
            System.out.println("The two PINs are different. Try again.");
        }
    }

    /**
     * Reads a matric number: exactly MATRIC_LENGTH digits. Students use it as their account ID.
     * @param sc the Scanner for keyboard input
     * @param prompt the text shown before the input
     * @return the matric number as text
     */
    public static String readMatricNumber(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (isDigits(line) && line.length() == MATRIC_LENGTH) {
                return line;
            }
            System.out.println("A matric number has exactly " + MATRIC_LENGTH + " digits, for example 25000001.");
        }
    }

    /** True when the text is not empty and every character is a digit 0 to 9. */
    private static boolean isDigits(String text) {
        if (text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
