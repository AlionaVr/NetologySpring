package org.parsers;

import org.Part;
import org.apache.commons.fileupload.MultipartStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class MultipartParser {
    private final Map<String, List<Part>> parts = new HashMap<>();
    private final int BUFFER_LIMIT = 4096;


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

    private static String extractParam(String header, String paramName) {
        // Content-Disposition: form-data; name="file"; filename="test.txt"
        for (String part : header.split(";")) {
            part = part.trim();
            if (part.startsWith(paramName + "=")) {
                return part.substring(paramName.length() + 1).replaceAll("^\"|\"$", "");
            }
        }
        return null;
    }

    public Map<String, List<Part>> parse(byte[] bodyBytes, String contentType) throws IOException {
        String boundary = findBoundary(contentType);
        if (boundary == null || boundary.isEmpty()) return parts;

        try (ByteArrayInputStream input = new ByteArrayInputStream(bodyBytes)) {
            MultipartStream multipartStream = new MultipartStream(input, boundary.getBytes(), BUFFER_LIMIT, null);

            boolean nextPart = multipartStream.skipPreamble();
            while (nextPart) {
                try {
                    Map<String, String> headers = parseHeaders(multipartStream.readHeaders());
                    String contentDisposition = headers.entrySet().stream()
                            .filter(e -> e.getKey().equalsIgnoreCase("Content-Disposition"))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(null);
                    if (contentDisposition == null) {
                        nextPart = multipartStream.readBoundary();
                        continue;
                    }

                    String name = extractParam(contentDisposition, "name");
                    String filename = extractParam(contentDisposition, "filename");
                    String contentTypeHeader = headers.get("Content-Type");

                    ByteArrayOutputStream partData = new ByteArrayOutputStream();
                    multipartStream.readBodyData(partData);

                    Part part = new Part(name, filename, contentTypeHeader, partData.toByteArray());
                    parts.computeIfAbsent(name, k -> new ArrayList<>()).add(part);

                    nextPart = multipartStream.readBoundary();
                } catch (IOException e) {
                    System.err.println("Error reading multipart part: " + e.getMessage());
                    break;
                }
            }
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
