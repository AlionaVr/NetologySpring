package org;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class Request {
    // must be in form GET /path HTTP/1.1
    private final String method;
    private final String path;


    public Request(String method, String path, String version, Map<String, String> headers, InputStream inputStream) {
        this.method = method;
        this.path = path;
    }

    public static Request fromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String startLine = reader.readLine();
        if (startLine == null || startLine.isBlank()) {
            throw new IOException("Empty request line");
        }

        String[] parts = startLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Invalid request line: " + startLine);

        }
        String method = parts[0];
        String path = parts[1];
        String version = parts[2];

        Map<String, String> headers = parseHeaders(reader);

        return new Request(method, path, version, headers, inputStream);
    }

    private static Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isBlank()) {
            if (!line.contains(":")) continue;
            String[] parts2 = line.split(":", 2);
            if (parts2.length == 2) {
                String name = parts2[0].trim();
                String value = parts2[1].trim();
                headers.put(name, value);
            }
        }
        return headers;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }
}

