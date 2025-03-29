package org.request;

import org.Part;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestBuilder {
    protected String method;
    protected String path;
    protected String version;
    protected Map<String, String> headers = new HashMap<>();
    protected Map<String, String> queryParams = new HashMap<>();
    protected byte[] bodyBytes = new byte[0];
    protected Map<String, List<Part>> postParams = new HashMap<>();

    public RequestBuilder method(String method) {
        this.method = method;
        return this;
    }

    public RequestBuilder path(String path) {
        this.path = path;
        return this;
    }

    public RequestBuilder version(String version) {
        this.version = version;
        return this;
    }

    public RequestBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public RequestBuilder queryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
        return this;
    }

    public RequestBuilder bodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
        return this;
    }

    public RequestBuilder postParams(Map<String, List<Part>> postParams) {
        this.postParams = postParams;
        return this;
    }

    public Request build() {
        return new Request(this);
    }
}

