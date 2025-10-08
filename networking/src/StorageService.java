package ru.mentee.power;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class StorageService {
    private static final Map<String, String> storage = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        initializeData();

        server.createContext("/data", exchange -> {
            try {
                String data = getAllData();
                System.out.println("Storage /data called, returning: " + data);
                String response = """
                    {
                        "data": %s,
                        "status": "success",
                        "timestamp": %d
                    }
                    """.formatted(data, System.currentTimeMillis());
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            } catch (Exception e) {
                System.out.println("Storage /data error: " + e.getMessage());
                String error = """
                    {
                        "error": "%s",
                        "status": "error",
                        "timestamp": %d
                    }
                    """.formatted(e.getMessage(), System.currentTimeMillis());
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
            } finally {
                exchange.close();
            }
        });

        server.createContext("/store", exchange -> {
            try {
                String method = exchange.getRequestMethod();
                if ("POST".equals(method)) {
                    String key = exchange.getRequestURI().getQuery();
                    if (key != null && key.contains("key=")) {
                        String keyValue = key.substring(key.indexOf("=") + 1);
                        String value = new String(exchange.getRequestBody().readAllBytes());
                        storage.put(keyValue, value);

                        String response = "{\"message\":\"Data stored successfully\",\"status\":\"success\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, response.length());
                        exchange.getResponseBody().write(response.getBytes());
                    } else {
                        String error = "{\"error\":\"Missing key parameter\",\"status\":\"error\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, error.length());
                        exchange.getResponseBody().write(error.getBytes());
                    }
                } else {
                    String error = "{\"error\":\"Method not allowed\",\"status\":\"error\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(405, error.length());
                    exchange.getResponseBody().write(error.getBytes());
                }
            } catch (Exception e) {
                String error = "{\"error\":\"" + e.getMessage() + "\",\"status\":\"error\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
            } finally {
                exchange.close();
            }
        });

        server.createContext("/health", exchange -> {
            String response = "{\"status\":\"UP\",\"service\":\"storage\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.start();
        System.out.println("Storage service started on port 8080");
    }

    private static void initializeData() {
        storage.put("user1", "John Doe");
        storage.put("user2", "Jane Smith");
        storage.put("config", "production");
        storage.put("version", "1.0.0");
    }

    private static String getAllData() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : storage.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

}