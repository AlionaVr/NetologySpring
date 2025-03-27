package org;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
}

