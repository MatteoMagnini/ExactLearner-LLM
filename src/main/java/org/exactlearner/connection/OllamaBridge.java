package org.exactlearner.connection;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

public class OllamaBridge extends BasicBridge {

    private int maxTokens = 100;
    private static final String defaultURL = System.getenv("EXACTLEARNER_OLLAMA_URL") == null ?
            "http://clusters.almaai.unibo.it:11434/api/generate" : System.getenv("EXACTLEARNER_OLLAMA_URL");

    public OllamaBridge(String model) {
        super();
        BasicBridge.model = model;
        BasicBridge.url = defaultURL;
    }

    public OllamaBridge(String model, int maxTokens) {
        super();
        BasicBridge.model = model;
        this.maxTokens = maxTokens;
        BasicBridge.url = defaultURL;
    }

    public OllamaBridge(String host, int port, String model, int maxTokens) {
        super();
        this.maxTokens = maxTokens;
        BasicBridge.model = model;
        BasicBridge.url = "http://" + host + ":" + port + "/api/generate";
    }

    public String ask(String message, String system) {
        try {
            HttpURLConnection connection = getConnection(url);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            String jsonInputString = "{\"model\": \"" + escapeJson(model) + "\",\n" +
                    "\"system\": \"" + escapeJson(system) + "\",\n" +
                    "\"options\": {\n\"num_predict\": " + maxTokens + "\n},\n" +
                    "\"stream\": false,\n" +
                    "\"prompt\": \"" + escapeJson(message) + "\"}";
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(jsonInputString);
            writer.flush();
            writer.close();
            String jsonResponse = getChatGPTResponse(connection);
            return extractMessageFromJSON(jsonResponse);
        } catch (Exception e) {
            System.out.println(ChatGPTCodes.valueOf(extractErrorCode(e.getMessage())));
            return null;
        }
    }

    private String extractMessageFromJSON(String json) {
        String key = "\"response\":\"";
        int start = json.indexOf(key) + key.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    public static String escapeJson(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20 || c > 0x7E) {
                        sb.append(String.format("\\u%04x", (int) c)); // Unicode escape for non-printable characters
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    @Override
    public String ask(String message, String key, String system) {
        return ask(message, system);
    }
}
