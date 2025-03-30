package org.parsers;

import org.Part;

import java.util.List;
import java.util.Map;

public interface BodyParser {
    Map<String, List<Part>> parse(byte[] bodyBytes, String contentType) throws java.io.IOException;
}
