package org;


import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        Server server = new Server(9999);

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            System.out.println("GET /messages handler called");
            String lastParam = request.getQueryParam("last");
            String body = "Last param = " + (lastParam != null ? lastParam : "not provided");
            byte[] content = body.getBytes();
            server.sendResponse(responseStream, 200, "OK", "text/plain", content.length);
            responseStream.write(content);
            responseStream.flush();
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            String received = request.getBodyAsString();
            String responseText = "Received POST: " + received;

            byte[] content = responseText.getBytes();
            server.sendResponse(responseStream, 200, "OK", "text/plain", content.length);
            responseStream.write(content);
            responseStream.flush();
        });

        // POST handler for form-urlencoded
        server.addHandler("POST", "/submit", (request, out) -> {
            String name = request.getPostParam("name");
            List<String> hobbies = request.getPostParams("hobby");

            String response = "Name: " + name + ", hobbies: " + String.join(", ", hobbies);
            byte[] content = response.getBytes();

            server.sendResponse(out, 200, "OK", "text/plain", content.length);
            out.write(content);
            out.flush();
        });

        new Thread(server::start).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Server interrupted");
        }

        Client client = new Client("localhost", 9999);
        try {
            client.sendGetRequest("/messages");
            client.sendGetRequest("/messages?last=123");
            client.sendPostRequest("/messages", "Hello from client POST request");
            client.sendPostRequest("/submit", "name=Aleh&hobby=gaming&hobby=coding");// Form-urlencoded POST
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}