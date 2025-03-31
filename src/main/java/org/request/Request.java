package org.request;

import org.apache.commons.fileupload.FileItem;
import org.parsers.BodyParser;
import org.parsers.FormUrlencodedBodyParser;
import org.parsers.HeaderParser;
import org.parsers.MultipartBodyParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Request {
    private static final int BUFFER_SIZE = 4096;

    private final String method;
    private final String path;
    private final String version;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final byte[] bodyBytes;
    private final Map<String, List<FileItem>> postParams;

    protected Request(RequestBuilder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.version = builder.version;
        this.headers = builder.headers;
        this.queryParams = builder.queryParams;
        this.bodyBytes = builder.bodyBytes;
        this.postParams = builder.postParams;
    }

    public static Request fromInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream requestLine = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;

        while ((read = in.read(buffer)) != -1) {
            requestLine.write(buffer, 0, read);
            if (requestLine.toString(StandardCharsets.UTF_8).contains("\r\n\r\n")) {
                break;
            }
        }

        byte[] requestLineByteArray = requestLine.toByteArray();
        String requestText = new String(requestLineByteArray, StandardCharsets.UTF_8);

        int headerEndIndex = requestText.indexOf("\r\n\r\n");
        if (headerEndIndex == -1) {
            throw new IOException("Invalid HTTP request: headers not terminated");
        }

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
        Map<String, String> queryParams = new HashMap<>();

        int questionIndex = fullPath.indexOf('?');
        if (questionIndex != -1) {
            path = fullPath.substring(0, questionIndex);
            String queryString = fullPath.substring(questionIndex + 1);
            queryParams = parseQueryParams(queryString);
        } else {
            path = fullPath;
        }

        HeaderParser headerParser = new HeaderParser();
        Map<String, String> headers = headerParser.parseHeaders(headerPart);

        ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
        int bodyStart = headerEndIndex + 4;   //after \r\n\r\n

        if (requestLineByteArray.length > bodyStart) {
            bodyBuffer.write(requestLineByteArray, bodyStart, requestLineByteArray.length - bodyStart);
        }

        byte[] bodyBytes = readBodyContent(in, headers, bodyBuffer);
        // Parse post parameters if applicable
        Map<String, List<FileItem>> postParams = parsePostParameters(method, headers, bodyBytes);

        return new RequestBuilder()
                .method(method)
                .path(path)
                .version(version)
                .headers(headers)
                .queryParams(queryParams)
                .bodyBytes(bodyBytes)
                .postParams(postParams)
                .build();
    }

    private static Map<String, List<FileItem>> parsePostParameters(String method, Map<String, String> headers, byte[] bodyBytes) throws IOException {
        Map<String, List<FileItem>> postParams = new HashMap<>();
        if ("POST".equalsIgnoreCase(method) && headers.containsKey("Content-Type")) {
            String contentType = headers.get("Content-Type");
            BodyParser parser = null;
            if (contentType.startsWith("application/x-www-form-urlencoded")) {
                parser = new FormUrlencodedBodyParser();
            } else if (contentType.startsWith("multipart/form-data")) {
                parser = new MultipartBodyParser();
            }
            postParams = parser != null ? parser.parse(bodyBytes, contentType) : null;
        }
        return postParams;
    }

    private static byte[] readBodyContent(InputStream in, Map<String, String> headers, ByteArrayOutputStream bodyBuffer) throws IOException {
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            int alreadyRead = bodyBuffer.size();
            int remaining = contentLength - alreadyRead;

            byte[] buffer = new byte[BUFFER_SIZE];
            while (remaining > 0) {
                int read = in.read(buffer, 0, Math.min(buffer.length, remaining));
                if (read == -1) break;
                bodyBuffer.write(buffer, 0, read);
                remaining -= read;
            }
        }
        return bodyBuffer.toByteArray();
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

    public FileItem getPostParam(String name) {
        List<FileItem> values = postParams.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public List<FileItem> getPostParams(String name) {
        return postParams.getOrDefault(name, Collections.emptyList());
    }
}