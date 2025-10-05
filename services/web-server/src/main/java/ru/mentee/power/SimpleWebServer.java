package ru.mentee.power;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class SimpleWebServer {
    private final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) throws IOException {
        new SimpleWebServer().start();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            String html = """
        <!DOCTYPE html>
        <html>
        <head><title>Web Server</title></head>
        <body>
            <h1>Web Server</h1>
            <p>Сервис для вызова Calculator API</p>
            <p><a href="/call-calculator?a=5&b=3&operation=add">Тест: 5 + 3</a></p>
            <p><a href="/health">Health Check</a></p>
        </body>
        </html>
        """;

            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            exchange.getResponseBody().write(html.getBytes());
            exchange.close();
        });

        server.createContext("/call-calculator", exchange -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String calculatorUrl = "http://calculator-service:8080/calculate?" + query;
                String result = callCalculatorService(calculatorUrl);

                String response = String.format(
                        "{\"web_server\":\"response\",\"calculator_result\":%s}",
                        result
                );

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            } catch (Exception e) {
                String error = "{\"error\":\"Service unavailable\"}";
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
            } finally {
                exchange.close();
            }
        });

        server.start();
        System.out.println("Web Server запущен на порту 8080");
    }

    private String callCalculatorService(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            return "{\"error\":\"Calculator service unavailable\"}";
        }
    }
}