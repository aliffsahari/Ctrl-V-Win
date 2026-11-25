public class SpendingAdvisor
{
    private AIHelper ai;

    public SpendingAdvisor(AIHelper ai)
    {
        this.ai = ai;
    }

    public String explainSpending(
            Account a,
            Transaction[] history)
            throws AIServiceException
    {
        String prompt = buildPrompt(a, history);

        String reply = ai.ask(prompt);

        AILogger.log(prompt, reply);

        return reply
                + "\n\nEducational only, not financial advice.";
    }

    private String buildPrompt(
            Account a,
            Transaction[] history)
    {
        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "You are a helpful financial literacy assistant for university students.\n\n");

        prompt.append("Student Information:\n");
        prompt.append("Name: ")
              .append(a.getName())
              .append("\n");

        prompt.append("Account ID: ")
              .append(a.getAccountId())
              .append("\n");

        prompt.append("Current Balance: RM ")
              .append(String.format("%.2f", a.getBalance()))
              .append("\n\n");

        prompt.append("Transaction History:\n");

        if (history != null)
        {
            for (Transaction t : history)
            {
                if (t != null)
                {
                    prompt.append("Date: ")
                          .append(t.getDateTime())
                          .append(", Type: ")
                          .append(t.getType())
                          .append(", Amount: RM ")
                          .append(String.format("%.2f", t.getAmount()))
                          .append(", Fee: RM ")
                          .append(String.format("%.2f", t.getFee()))
                          .append(", Status: ")
                          .append(t.getStatus())
                          .append("\n");
                }
            }
        }

        prompt.append("\n");

        prompt.append(
                "Based on the transaction history above, provide:\n");
        prompt.append(
                "1. A short spending summary.\n");
        prompt.append(
                "2. The most noticeable spending pattern.\n");
        prompt.append(
                "3. One practical money-saving suggestion.\n");
        prompt.append(
                "4. A warning if the spending behaviour appears excessive.\n");
        prompt.append(
                "Keep the response under 150 words and easy for a university student to understand.");

        return prompt.toString();
    }
}