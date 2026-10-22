package lanse.worldwarchess.shared.chat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class ChatFilter {

    private record FilterRule(int length, String replacement) {}

    private static final Map<String, FilterRule> FILTERS = new HashMap<>();

    static {
        // le **** F O R B I D D E N **** words
        FILTERS.put("08a841e996781e9e77d30a4e4420a8f501a280b00624e6d1224bf54aaff73eba", new FilterRule(5, "cooka"));
        FILTERS.put("bb61ef40814ce34c1edf0edb609854be9793198a8f60b67d9fb26643c32281d3", new FilterRule(6, "cooker"));
        FILTERS.put("120f6e5b4ea32f65bda68452fcfaaef06b0136e1d0e4a6f60bc3771fa0936dd6", new FilterRule(6, "cooker"));
        FILTERS.put("2c598436e5575a5769b69986014588d52c2698414b623e81b2e776766c30eaba", new FilterRule(6, "fr*nce"));
    }

    //cleaning lady magic
    public static String cleanInput(String input) {
        if (input == null || input.isBlank()) return input;

        String lower = input.toLowerCase();
        StringBuilder output = new StringBuilder(input);

        for (int i = 0; i < lower.length(); i++) {
            for (FilterRule rule : FILTERS.values()) {
                int len = rule.length;
                if (i + len > lower.length()) continue;

                String sub = lower.substring(i, i + len);
                String hash = sha256(sub);

                FilterRule match = FILTERS.get(hash);
                if (match != null) {
                    output.replace(i, i + len, match.replacement);
                    lower = output.toString().toLowerCase();
                }
            }
        }

        return output.toString();
    }

    private static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}