package org;

import org.request.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    // private static final List<String> VALID_PATHS = List.of(
    //         "/index.html", "/spring.svg", "/spring.png",
    //         "/resources.html", "/styles.css", "/app.js",
    //         "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final int PORT;
    private final String PUBLIC_DIR = "public";
    private final String CLASSIC_HTML_PATH = "/classic.html";
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server(int port) {
        this.PORT = port;
    }

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
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {

            Request request = Request.fromInputStream(socket.getInputStream());
            String path = request.getPath();
            String method = request.getMethod();

            Handler handler = Optional.ofNullable(handlers.get(method))
                    .map(h -> h.get(path))
                    .orElse(null);

            if (handler != null) {
                handler.handle(request, out);
                return;
            }

            Path filePath = Path.of(".", PUBLIC_DIR, path);
            if (!Files.exists(filePath)) {
                Response.notFound().send(out);
                return;
            }

            String mimeType = Files.probeContentType(filePath);

            if (path.equals(CLASSIC_HTML_PATH)) {
                String template = Files.readString(filePath);
                String content = template.replace("{time}", LocalDateTime.now().toString());
                Response.ok(mimeType, content).send(out);
            } else {
                byte[] content = Files.readAllBytes(filePath);
                Response.ok(mimeType, content).send(out);
            }
        } catch (IOException e) {
            System.err.println("Connection handling error: " + e.getMessage());
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>())
                .put(path, handler);
    }
}

