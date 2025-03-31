package org.parsers;

import org.apache.commons.fileupload.FileItem;

import java.util.List;
import java.util.Map;

public interface BodyParser {
    Map<String, List<FileItem>> parse(byte[] bodyBytes, String contentType) throws java.io.IOException;
}
