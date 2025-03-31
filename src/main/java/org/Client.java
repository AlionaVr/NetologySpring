package org;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class Client {
    private final String HOST;
    private final int PORT;
    private final HttpClient httpClient;

    public Client(String host, int port) {
        this.HOST = host;
        this.PORT = port;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void sendGetRequest(String path) throws IOException, InterruptedException {
        String urlString = "http://" + HOST + ":" + PORT + path;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("GET Response: " + response);
        System.out.println(response.body());
    }

    public void sendPostRequest(String path, String body) throws IOException, InterruptedException {
        String urlString = "http://" + HOST + ":" + PORT + path;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("POST Response Code: " + response.statusCode());
        System.out.println(response.body());

    }

    public void sendMultipartPostRequest(String path) throws IOException, InterruptedException {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String CRLF = "\r\n";
        String twoHyphens = "--";

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Поле "name"
        output.write((twoHyphens + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"name\"" + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: text/plain; charset=UTF-8" + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(CRLF.getBytes(StandardCharsets.UTF_8));
        output.write("Aleh".getBytes(StandardCharsets.UTF_8));
        output.write(CRLF.getBytes(StandardCharsets.UTF_8));

        // Поле "file"
        output.write((twoHyphens + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"file\"; filename=\"hello.txt\"" + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: text/plain" + CRLF).getBytes(StandardCharsets.UTF_8));
        output.write(CRLF.getBytes(StandardCharsets.UTF_8));
        output.write("This is the content of the file.".getBytes(StandardCharsets.UTF_8));
        output.write(CRLF.getBytes(StandardCharsets.UTF_8));

        // Конечный boundary
        output.write((twoHyphens + boundary + twoHyphens + CRLF).getBytes(StandardCharsets.UTF_8));

        byte[] multipartBody = output.toByteArray();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + HOST + ":" + PORT + path))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("POST /upload response: " + response.statusCode());
        System.out.println(response.body());

    }
}

