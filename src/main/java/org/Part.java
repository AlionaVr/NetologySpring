package org;

public class Part {
    private final String name;
    private final String filename;
    private final String contentType;
    private final byte[] data;

    public Part(String name, String filename, String contentType, byte[] data) {
        this.name = name;
        this.filename = filename;
        this.contentType = contentType;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentAsString() {
        return new String(data);
    }

    public boolean isFile() {
        return filename != null;
    }

    @Override
    public String toString() {
        return isFile() ? filename : getContentAsString();
    }
}
