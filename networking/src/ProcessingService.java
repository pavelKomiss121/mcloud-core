package ru.mentee.power;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;

public class ProcessingService {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        HttpClient client = HttpClient.newHttpClient();

        server.createContext("/process", exchange -> {
            try {
                HttpRequest storageRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://storage:8080/data"))
                        .GET()
                        .build();

                HttpResponse<String> storageResponse = client.send(storageRequest, HttpResponse.BodyHandlers.ofString());

                String data = storageResponse.body();
                System.out.println("Storage response: " + data);

                String result = "{\"processed\":\"" + data + "\",\"status\":\"success\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, result.length());
                exchange.getResponseBody().write(result.getBytes());

            } catch (IOException | InterruptedException e) {
                String error = "{\"error\":\"" + e.getMessage() + "\",\"status\":\"error\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
            } finally {
                exchange.close();
            }
        });

        server.createContext("/health", exchange -> {
            String response = """
                {
                    "status": "UP",
                    "service": "processing",
                    "timestamp": %d,
                    "version": "1.0.0"
                }
                """.formatted(System.currentTimeMillis());
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.start();
        System.out.println("Processing service started on port 8080");
    }
}