package org;


public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            System.out.println("GET /messages handler called");
            String body = "Message (GET)";
            byte[] content = body.getBytes();
            server.sendResponse(responseStream, 200, "OK", "text/plain", content.length);
            responseStream.write(content);
            responseStream.flush();
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            System.out.println("POST /messages handler called");
            String body = "Message (POST)";
            byte[] content = body.getBytes();
            server.sendResponse(responseStream, 200, "OK", "text/plain", content.length);
            responseStream.write(content);
            responseStream.flush();
        });
        server.start();
    }
}