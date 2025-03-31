package org;


import org.apache.commons.fileupload.FileItem;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        final int PORT = 9999;
        Server server = new Server(PORT);

        server.addHandler("GET", "/messages", (request, out) -> {
            System.out.println("GET /messages handler called");
            String lastParam = request.getQueryParam("last");
            String body = "Last param = " + (lastParam != null ? lastParam : "not provided");
            Response.ok("text/plain", body).send(out);
        });

        server.addHandler("POST", "/messages", (request, out) -> {
            String received = request.getBodyAsString();
            String responseText = "Received POST: " + received;
            Response.ok("text/plain", responseText).send(out);
        });

        // POST handler for form-urlencoded
        server.addHandler("POST", "/submit", (request, out) -> {
            FileItem namePart = request.getPostParam("name");
            List<FileItem> hobbies = request.getPostParams("hobby");

            String name = namePart != null ? namePart.toString() : "unknown";
            String hobbiesText = hobbies.stream()
                    .map(FileItem::toString)
                    .collect(Collectors.joining(", "));

            String response = "Name: " + name + ", hobbies: " + hobbiesText;

            Response.ok("text/plain", response).send(out);
        });

        // POST handler for multipart
        server.addHandler("POST", "/upload", (request, out) -> {
            FileItem nameItem = request.getPostParam("name");
            FileItem fileItem = request.getPostParam("file");

            String name = nameItem != null ? nameItem.getString() : "unknown";
            String filename = fileItem != null ? fileItem.getName() : "no file";
            long size = fileItem != null ? fileItem.getSize() : 0;

            String response = "Uploaded by: " + name + "<br>Filename: " + filename + "<br>Size: " + size + " bytes";

            Response.ok("text/html", response).send(out);
        });

        new Thread(server::start).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Server interrupted");
        }
        testClient(PORT);

    }

    private static void testClient(int port) {
        Client client = new Client("localhost", port);
        try {
            System.out.println("\nTesting GET /messages");
            client.sendGetRequest("/messages");

            System.out.println("\nTesting GET /messages?last=123");
            client.sendGetRequest("/messages?last=123");

            System.out.println("\nTesting POST /messages");
            client.sendPostRequest("/messages", "Hello from client POST request");

            System.out.println("\nTesting POST /submit (form-urlencoded)");
            client.sendPostRequest("/submit", "name=Aleh&hobby=gaming&hobby=coding");// Form-urlencoded POST

            System.out.println("\nTesting POST /upload (multipart-form-data)");
            client.sendMultipartPostRequest("/upload");

        } catch (IOException | InterruptedException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}