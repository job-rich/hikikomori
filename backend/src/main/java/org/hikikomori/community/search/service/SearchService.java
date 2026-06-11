package org.hikikomori.community.search.service;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    public List<String> tokenize(String query) {
        if (query == null || query.isBlank()) return List.of();
        return Arrays.stream(query.split("\\s+"))
                .filter(t -> !t.isBlank())
                .toList();
    }

    public String snippet(String content, String query, int window) {
        if (content == null || content.isBlank()) return "";

        List<String> tokens = tokenize(query);
        int matchPos = -1;
        if (!tokens.isEmpty()) {
            String lower = content.toLowerCase();
            for (String token : tokens) {
                int pos = lower.indexOf(token.toLowerCase());
                if (pos >= 0) {
                    matchPos = pos;
                    break;
                }
            }
        }

        int start, end;
        boolean prefixEllipsis, suffixEllipsis;
        if (matchPos >= 0) {
            start = Math.max(0, matchPos - window);
            end = Math.min(content.length(), matchPos + window);
            prefixEllipsis = start > 0;
            suffixEllipsis = end < content.length();
        } else {
            start = 0;
            end = Math.min(content.length(), window * 2);
            prefixEllipsis = false;
            suffixEllipsis = end < content.length();
        }

        StringBuilder sb = new StringBuilder();
        if (prefixEllipsis) sb.append("…");
        sb.append(content, start, end);
        if (suffixEllipsis) sb.append("…");
        return sb.toString();
    }
}
