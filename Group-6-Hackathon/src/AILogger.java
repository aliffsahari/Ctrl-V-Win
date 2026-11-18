import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Appends every AI reply shown to a user to ai_log.txt.
 */
public class AILogger {

    private static final String LOG_FILE = "ai_log.txt";

    /**
     * Writes one timestamped entry to the log file.
     * A failure to write is reported but never stops the program.
     * @param prompt the prompt that was sent
     * @param reply the reply that was shown to the user
     */
    public static void log(String prompt, String reply) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println("[" + LocalDateTime.now() + "] PROMPT: " + prompt.replace('\n', ' '));
            pw.println("[" + LocalDateTime.now() + "] REPLY: " + reply.replace('\n', ' '));
        } catch (IOException e) {
            System.out.println("(Could not write to " + LOG_FILE + ")");
        }
    }
}
