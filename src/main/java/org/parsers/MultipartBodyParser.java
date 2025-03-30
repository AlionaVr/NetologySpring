package org.parsers;

import org.Part;
import org.apache.commons.fileupload.MultipartStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class MultipartBodyParser implements BodyParser {
    private final Map<String, List<Part>> parts = new HashMap<>();
    private final int BUFFER_LIMIT = 4096;

    @Override
    public Map<String, List<Part>> parse(byte[] bodyBytes, String contentType) throws IOException {
        HeaderParser headerParser = new HeaderParser();

        String boundary = headerParser.findBoundary(contentType);
        if (boundary == null || boundary.isEmpty()) return parts;

        try (ByteArrayInputStream input = new ByteArrayInputStream(bodyBytes)) {
            MultipartStream multipartStream = new MultipartStream(input, boundary.getBytes(), BUFFER_LIMIT, null);

            boolean nextPart = multipartStream.skipPreamble();
            while (nextPart) {
                try {
                    Map<String, String> headers = headerParser.parseHeaders(multipartStream.readHeaders());
                    String contentDisposition = headers.entrySet().stream()
                            .filter(e -> e.getKey().equalsIgnoreCase("Content-Disposition"))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(null);
                    if (contentDisposition == null) {
                        nextPart = multipartStream.readBoundary();
                        continue;
                    }

                    String name = headerParser.extractParam(contentDisposition, "name");
                    String filename = headerParser.extractParam(contentDisposition, "filename");
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
        return (list != null && !list.isEmpty()) ? list.getFirst() : null;
    }

    public List<Part> getParts(String name) {
        return parts.getOrDefault(name, Collections.emptyList());
    }
}
