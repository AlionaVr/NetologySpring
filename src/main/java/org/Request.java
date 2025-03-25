package org;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;
    private final InputStream inputStream;

    public Request(String method, String path, String version, Map<String, String> headers,
                   InputStream inputStream, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.inputStream = inputStream;
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
        String fullPath = parts[1];    // fullPath = '/search?key1=value1&k2=v2'
        String version = parts[2];

        String path;
        Map<String, String> queryParams;

        int questionIndex = fullPath.indexOf('?');
        if (questionIndex != -1) {
            path = fullPath.substring(0, questionIndex);
            String queryString = fullPath.substring(questionIndex + 1);
            queryParams = parseQueryParams(queryString);
        } else {
            path = fullPath;
            queryParams = new HashMap<>();
        }

        Map<String, String> headers = parseHeaders(reader);

        return new Request(method, path, version, headers, inputStream, queryParams);
    }

    private static Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        String[] params = queryString.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                queryParams.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8), URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            } else if (keyValue.length == 1) {
                queryParams.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8), "");
            }
        }
        return queryParams;
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

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getBodyAsString() throws IOException {
        byte[] bodyBytes = inputStream.readAllBytes();
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }
}

