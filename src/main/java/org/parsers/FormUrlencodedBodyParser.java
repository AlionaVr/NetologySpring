package org.parsers;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormUrlencodedBodyParser implements BodyParser {
    @Override
    public Map<String, List<FileItem>> parse(byte[] bodyBytes, String contentType) throws IOException {
        Map<String, List<FileItem>> postParams = new HashMap<>();
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        String[] paramPairs = body.split("&");
        DiskFileItemFactory factory = new DiskFileItemFactory();

        for (String pair : paramPairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);

                FileItem item = factory.createItem(key, "text/plain", false, null);

                try (OutputStream out = item.getOutputStream()) {
                    out.write(value.getBytes(StandardCharsets.UTF_8));
                }

                postParams.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
            }
        }
        return postParams;
    }
}
