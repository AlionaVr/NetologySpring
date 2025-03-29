package org.parsers;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Response {

    private final int statusCode;
    private final String statusText;
    private final Map<String, String> headers;
    private byte[] body;

    public Response(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = new HashMap<>();
        this.body = new byte[0];
    }

    public static Response ok(String contentType, byte[] body) {
        return new Response(200, "OK")
                .setContentType(contentType)
                .setBody(body);
    }

    public static Response ok(String contentType, String body) {
        return new Response(200, "OK")
                .setContentType(contentType)
                .setBody(body);
    }

    public static Response notFound() {
        return new Response(404, "Not Found")
                .setContentType("text/plain")
                .setBody("404 Not Found");
    }

    public static Response serverError(String message) {
        return new Response(500, "Internal Server Error")
                .setContentType("text/plain")
                .setBody("500 Internal Server Error: " + message);
    }

    public Response setContentType(String contentType) {
        headers.put("Content-Type", contentType);
        return this;
    }

    private Response setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(this.body.length));
        return this;
    }

    public Response setBody(byte[] body) {
        this.body = body;
        headers.put("Content-Length", String.valueOf(body.length));
        return this;
    }

    public void send(BufferedOutputStream out) throws IOException {
        StringBuilder responseText = new StringBuilder();
        // status line
        responseText.append("HTTP/1.1 ")
                .append(statusCode)
                .append(" ")
                .append(statusText)
                .append("\r\n");
        //headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            responseText.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        if (!headers.containsKey("Connection")) {
            responseText.append("Connection: close\r\n");
        }
        responseText.append("\r\n");


        out.write(responseText.toString().getBytes(StandardCharsets.UTF_8));
        // Write body if exists
        if (body.length > 0) {
            out.write(body);
        }
        out.flush();
    }
}