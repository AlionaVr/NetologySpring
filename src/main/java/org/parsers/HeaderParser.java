package org.parsers;

import java.util.HashMap;
import java.util.Map;

public class HeaderParser {
    public Map<String, String> parseHeaders(String headerText) {
        Map<String, String> headers = new HashMap<>();
        String[] lines = headerText.split("\r\n");
        for (String line : lines) {
            int index = line.indexOf(":");
            if (index != -1) {
                String name = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                headers.put(name, value);
            }
        }
        return headers;
    }

    protected String extractParam(String header, String paramName) {
        // Content-Disposition: form-data; name="file"; filename="test.txt"
        for (String part : header.split(";")) {
            part = part.trim();
            if (part.startsWith(paramName + "=")) {
                return part.substring(paramName.length() + 1).replaceAll("^\"|\"$", "");
            }
        }
        return null;
    }

    protected String findBoundary(String contentType) {
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            for (String param : contentType.split(";")) {
                param = param.trim();
                if (param.startsWith("boundary=")) {
                    return param.substring("boundary=".length()).replace("\"", "");
                }
            }
        }
        return null;
    }
}
