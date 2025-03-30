package org.parsers;

import org.Part;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormUrlencodedBodyParser implements BodyParser {
    @Override
    public Map<String, List<Part>> parse(byte[] bodyBytes, String contentType) throws IOException {
        Map<String, List<Part>> postParams = new HashMap<>();
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        String[] paramPairs = body.split("&");

        for (String pair : paramPairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);

                byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
                Part part = new Part(key, null, "text/plain", valueBytes);

                postParams.computeIfAbsent(key, k -> new ArrayList<>()).add(part);
            }
        }
        return postParams;
    }
}
