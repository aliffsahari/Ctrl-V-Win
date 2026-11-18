import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Single entry point for every AI request in the program.
 * Reads settings from config.txt, sends the request with HttpURLConnection,
 * and builds / parses JSON with plain String methods (no external libraries).
 *
 * NOTE: the JSON below follows the common "chat completions" format.
 * Check your provider's documentation (e.g. ILMUCode) and adjust
 * buildRequestJson() and extractReply() if its format differs.
 */
public class AIHelper {

    private static final String CONFIG_FILE = "config.txt";
    private static final int TIMEOUT_MS = 15000;

    private String apiKey;
    private String apiUrl;
    private String model;

    /**
     * Creates the helper and loads settings from config.txt.
     * Does not throw: missing settings are reported later by ask().
     */
    public AIHelper() {
        loadConfig();
    }

    /**
     * Sends a prompt to the AI service and returns the reply text.
     * @param prompt the question or instruction for the AI
     * @return the AI reply as plain text
     * @throws AIServiceException if the key, URL or connection is unavailable,
     *                            or the reply cannot be read
     */
    public String ask(String prompt) throws AIServiceException {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key-here")) {
            throw new AIServiceException("API key not found in " + CONFIG_FILE + ".");
        }
        if (apiUrl == null || apiUrl.isEmpty() || model == null || model.isEmpty()) {
            throw new AIServiceException("API_URL or MODEL missing in " + CONFIG_FILE + ".");
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            String body = buildRequestJson(prompt);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(body.getBytes("UTF-8"));
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new AIServiceException("AI service returned HTTP " + status + ".");
            }

            String response = readAll(conn);
            return extractReply(response);

        } catch (IOException e) {
            throw new AIServiceException("Cannot reach the AI service (check internet).", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /** Reads settings (KEY=value lines) from config.txt. */
    private void loadConfig() {
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq < 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (key.equals("API_KEY")) {
                    apiKey = value;
                } else if (key.equals("API_URL")) {
                    apiUrl = value;
                } else if (key.equals("MODEL")) {
                    model = value;
                }
            }
        } catch (IOException e) {
            // File missing or unreadable: leave fields null; ask() will throw.
        }
    }

    /** Builds the JSON request body by hand. */
    private String buildRequestJson(String prompt) {
        return "{\"model\":\"" + escapeJson(model) + "\","
             + "\"messages\":[{\"role\":\"user\",\"content\":\""
             + escapeJson(prompt) + "\"}]}";
    }

    /** Escapes characters that would break a JSON string. */
    private String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Reads the whole HTTP response body as text. */
    private String readAll(HttpURLConnection conn) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Pulls the reply text out of the JSON using indexOf / substring.
     * Looks for the first "content":"..." and unescapes it.
     */
    private String extractReply(String json) throws AIServiceException {
        String marker = "\"content\"";
        int pos = json.indexOf(marker);
        if (pos < 0) {
            throw new AIServiceException("Unexpected reply format from AI service.");
        }
        int colon = json.indexOf(':', pos + marker.length());
        int start = json.indexOf('"', colon + 1);
        if (colon < 0 || start < 0) {
            throw new AIServiceException("Unexpected reply format from AI service.");
        }
        start++;

        StringBuilder sb = new StringBuilder();
        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char n = json.charAt(i + 1);
                switch (n) {
                    case 'n':  sb.append('\n'); break;
                    case 't':  sb.append('\t'); break;
                    case 'r':  break;
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/');  break;
                    default:   sb.append(n);
                }
                i += 2;
            } else if (c == '"') {
                return sb.toString().trim();
            } else {
                sb.append(c);
                i++;
            }
        }
        throw new AIServiceException("Incomplete reply from AI service.");
    }
}
