package org;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final List<String> VALID_PATHS = List.of(
            "/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js",
            "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private static final int PORT = 9999;
    private static final String PUBLIC_DIR = "public";
    private static final String CLASSIC_HTML_PATH = "/classic.html";

    protected void start() {
        ExecutorService executor = Executors.newFixedThreadPool(64);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    executor.execute(() -> handleConnection(socket));
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            // must be in form GET /path HTTP/1.1
            Optional<String> requestLine = Optional.ofNullable(in.readLine());
            if (requestLine.isEmpty()) {
                return;
            }
            String path = getPathFromRequest(requestLine);

            if (path == null || !VALID_PATHS.contains(path)) {
                sendResponse(out, 404, "Not Found", null, 0);
                out.flush();
                return;
            }

            Path filePath = Path.of(".", PUBLIC_DIR, path);
            String mimeType = Files.probeContentType(filePath);

            if (path.equals(CLASSIC_HTML_PATH)) {
                sendForClassicHtml(out, filePath, mimeType);
            } else {
                sendDefaultFile(out, filePath, mimeType);
            }
        } catch (IOException e) {
            System.err.println("Connection handling error: " + e.getMessage());
        }
    }

    private String getPathFromRequest(Optional<String> requestLine) {
        String[] parts = requestLine.get().split(" ");
        if (parts.length != 3) {
            return null;
        }
        return parts[1];
    }

    private void sendForClassicHtml(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        String template = Files.readString(filePath);
        byte[] content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();

        sendResponse(out, 200, "OK", mimeType, content.length);
        out.write(content);
        out.flush();

    }

    private void sendDefaultFile(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        long length = Files.size(filePath);
        sendResponse(out, 200, "OK", mimeType, length);
        Files.copy(filePath, out);
        out.flush();
    }

    private void sendResponse(BufferedOutputStream out, int statusCode, String statusText,
                              String mimeType, long contentLength) throws IOException {
        StringBuilder headers = new StringBuilder();
        headers.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");

        if (mimeType != null) {
            headers.append("Content-Type: ").append(mimeType).append("\r\n");
        }
        headers.append("Content-Length: ").append(contentLength).append("\r\n")
                .append("Connection: close\r\n")
                .append("\r\n");

        out.write(headers.toString().getBytes());
    }
}

