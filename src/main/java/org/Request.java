package org;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;
    private static final int limit = 4096;
    private final byte[] bodyBytes;

    public Request(String method, String path, String version, Map<String, String> headers,
                   byte[] bodyBytes, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.bodyBytes = bodyBytes;
    }

    public static Request fromInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream requestLine = new ByteArrayOutputStream();
        byte[] buffer = new byte[limit];
        int read;

        while ((read = in.read(buffer)) != -1) {
            requestLine.write(buffer, 0, read);
            if (requestLine.toString().contains("\r\n\r\n")) {
                break;
            }
        }
        byte[] requestLineByteArray = requestLine.toByteArray();
        String requestText = new String(requestLineByteArray, StandardCharsets.UTF_8);

        int headerEndIndex = requestText.indexOf("\r\n\r\n");
        String headerPart = requestText.substring(0, headerEndIndex);
        String[] headerLines = headerPart.split("\r\n");

        String[] requestLineParts = headerLines[0].split(" ");//"POST /messages?last=10 HTTP/1.1"
        if (requestLineParts.length != 3) {
            throw new IOException("Invalid request line: " + headerPart);
        }

        String method = requestLineParts[0];
        String fullPath = requestLineParts[1]; // fullPath = '/search?key1=value1&k2=v2'
        String version = requestLineParts[2];

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

        Map<String, String> headers = parseHeaders(headerLines);

        ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
        int bodyStart = headerEndIndex + 4;   //after \r\n\r\n

        if (requestLineByteArray.length > bodyStart) {
            bodyBuffer.write(requestLineByteArray, bodyStart, requestLineByteArray.length - bodyStart);
        }

        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            while (bodyBuffer.size() < contentLength) {
                read = in.read(buffer);
                if (read == -1) break;
                bodyBuffer.write(buffer, 0, read);
            }
        }
        byte[] bodyBytes = bodyBuffer.toByteArray();

        return new Request(method, path, version, headers, bodyBytes, queryParams);
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


    private static Map<String, String> parseHeaders(String[] lines) {
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String[] headerParts = lines[i].split(":", 2);
            if (headerParts.length == 2) {
                String name = headerParts[0].trim();
                String value = headerParts[1].trim();
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
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }
}

