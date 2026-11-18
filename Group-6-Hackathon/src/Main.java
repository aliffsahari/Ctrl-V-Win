import java.util.Scanner;

/**
 * Program entry point. Currently a minimal text menu that proves the
 * Scanner loop and the AI fallback work. Replace with your real menus.
 */
public class Main {

    /**
     * Runs the main menu loop until the user chooses to exit.
     * @param args not used
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AIHelper ai = new AIHelper();
        boolean running = true;

        while (running) {
            System.out.println("\n=== Group 6 Hackathon ===");
            System.out.println("1. Ask the AI a test question");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    askAI(sc, ai);
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1 or 0.");
            }
        }
        System.out.println("Goodbye!");
        sc.close();
    }

    /**
     * Sends a test prompt through AIHelper, logs the reply, and shows a
     * fallback message if the AI service is unavailable.
     */
    private static void askAI(Scanner sc, AIHelper ai) {
        System.out.print("Enter your question: ");
        String prompt = sc.nextLine().trim();
        if (prompt.isEmpty()) {
            System.out.println("Question cannot be empty.");
            return;
        }
        try {
            String reply = ai.ask(prompt);
            System.out.println("\nAI: " + reply);
            AILogger.log(prompt, reply);
        } catch (AIServiceException e) {
            System.out.println("\nAI feature unavailable: " + e.getMessage());
            System.out.println("The rest of the program still works.");
        }
    }
}
