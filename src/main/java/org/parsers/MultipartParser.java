package org.parsers;

import org.Part;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class MultipartParser {
    private final Map<String, List<Part>> parts = new HashMap<>();

    private static Map<String, String> parseHeaders(String headerText) {
        Map<String, String> headers = new HashMap<>();
        String[] lines = headerText.split("\r\n");
        for (String line : lines) {
            int colonIndex = line.indexOf(":");
            if (colonIndex != -1) {
                String name = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                headers.put(name, value);
            }
        }
        return headers;
    }

    private static String findBoundary(String contentType) {
        if (contentType != null) {
            for (String param : contentType.split(";")) {
                param = param.trim();
                if (param.startsWith("boundary=")) {
                    return param.substring("boundary=".length());
                }
            }
        }
        return null;
    }

    private static String extractParam(String line, String paramName) {
        // Content-Disposition: form-data; name="file"; filename="test.txt"
        String[] parts = line.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith(paramName + "=")) {
                return part.substring(5).replaceAll("^\"|\"$", "");
            }
        }
        return null;
    }

    public Map<String, List<Part>> parse(byte[] bodyBytes, String contentType) {
        String boundary = findBoundary(contentType);
        if (boundary == null) return parts;

        String delimiter = "--" + boundary;

        String bodyText = new String(bodyBytes, StandardCharsets.UTF_8);
        String[] rawParts = bodyText.split(delimiter);

        for (String rawPart : rawParts) {
            rawPart = rawPart.strip();
            if (rawPart.isEmpty() || rawPart.equals("--")) continue;
            int headerEndIndex = rawPart.indexOf("\r\n\r\n");
            if (headerEndIndex == -1) continue;
            String headerPart = rawPart.substring(0, headerEndIndex);
            String bodyPart = rawPart.substring(headerEndIndex + 4);

            Map<String, String> headers = parseHeaders(headerPart);
            String contentDisposition = headers.get("Content-Disposition");
            if (contentDisposition == null) continue;

            String name = extractParam(contentDisposition, "name");
            String filename = extractParam(contentDisposition, "filename");
            String contentTypeHeader = headers.get("Content-Type");

            byte[] data = bodyPart.getBytes(StandardCharsets.UTF_8);

            Part part = new Part(name, filename, contentTypeHeader, data);
            parts.computeIfAbsent(name, k -> new ArrayList<>()).add(part);
        }
        return parts;
    }

    public Part getPart(String name) {
        List<Part> list = parts.get(name);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    public List<Part> getParts(String name) {
        return parts.getOrDefault(name, Collections.emptyList());
    }
}
