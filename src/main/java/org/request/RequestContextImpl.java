package org.request;

import org.apache.commons.fileupload.RequestContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class RequestContextImpl implements RequestContext {
    private final Map<String, String> headers;
    private final byte[] body;

    public RequestContextImpl(Map<String, String> headers, byte[] body) {
        this.headers = headers;
        this.body = body;
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public String getContentType() {
        return headers.getOrDefault("Content-Type", null);
    }

    @Override
    public int getContentLength() {
        return -1;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(body);
    }
}
