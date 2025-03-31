package org.parsers;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.request.RequestContextImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartBodyParser implements BodyParser {

    @Override
    public Map<String, List<FileItem>> parse(byte[] bodyBytes, String contentType) throws IOException {
        Map<String, List<FileItem>> postParams = new HashMap<>();

        RequestContext context = new RequestContextImpl(
                Map.of("Content-Type", contentType),
                bodyBytes
        );

        FileUpload upload = new FileUpload(new DiskFileItemFactory());

        try {
            List<FileItem> items = upload.parseRequest(context);
            for (FileItem item : items) {
                postParams
                        .computeIfAbsent(item.getFieldName(), k -> new ArrayList<>())
                        .add(item);
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse multipart request", e);
        }

        return postParams;
    }
}

