/**
 * Thrown by AIHelper when the AI service cannot be used,
 * for example a missing API key, no internet or an unreadable reply.
 * Callers catch it, show a fallback message and keep the program running.
 */
public class AIServiceException extends Exception {

    /**
     * Creates the exception with a message describing the problem.
     * @param message what went wrong
     */
    public AIServiceException(String message) {
        super(message);
    }

    /**
     * Creates the exception with a message and the original cause.
     * @param message what went wrong
     * @param cause the underlying exception
     */
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
