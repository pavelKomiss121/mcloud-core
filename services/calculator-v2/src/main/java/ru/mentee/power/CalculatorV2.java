package ru.mentee.power;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class CalculatorV2 {

    private static int requestCount = 0;
    private static long totalProcessingTime = 0;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/calculate", exchange -> {
            long startTime = System.currentTimeMillis();
            try {
                String query = exchange.getRequestURI().getQuery();
                String[] params = query.split("&");
                Double a = Double.parseDouble(params[0].split("=")[1]);
                Double b = Double.parseDouble(params[1].split("=")[1]);
                String operator = params[2].split("=")[1];
                Double result = 0.0;

                switch (operator) {
                    case "add": result = a + b; break;
                    case "subtract": result = a - b; break;
                    case "multiply": result = a * b; break;
                    case "divide": result = a / b; break;
                    case "pow": result = Math.pow(a, b); break;
                    case "sqrt": result = Math.sqrt(a); break;
                }
                long processingTime = System.currentTimeMillis() - startTime;
                totalProcessingTime += processingTime;

                String hostname = System.getenv("HOSTNAME");
                if (hostname == null) hostname = "localhost";

                String response = String.format(
                        "{\"result\":%.2f,\"version\":\"v2\",\"pod\":\"%s\",\"precision\":\"high\"}",
                        result, hostname
                );
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());

                requestCount++;

            } catch (Exception e) {
                String error = "{\"error\":\"Invalid parameters\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(400, error.length());
                exchange.getResponseBody().write(error.getBytes());
            } finally {
                exchange.close();
            }
        });

        server.createContext("/health", exchange -> {
            String response = "{\"status\":\"UP\",\"service\":\"calculator-v2\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.createContext("/metrics", exchange -> {
            try {
                String hostname = System.getenv("HOSTNAME");
                if (hostname == null) hostname = "localhost";

                double avgProcessingTime = requestCount > 0 ? (double)totalProcessingTime / requestCount : 0;

                String metrics = String.format(
                        "{\"requests_total\":%d,\"avg_processing_time\":%.2f,\"version\":\"v2\",\"pod\":\"%s\",\"memory_used\":%d}",
                        requestCount,
                        avgProcessingTime,
                        hostname,
                        Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                );

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, metrics.length());
                exchange.getResponseBody().write(metrics.getBytes());
            } catch (Exception e) {
                String error = "{\"error\":\"Failed to get metrics\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
            } finally {
                exchange.close();
            }
        });

        server.start();
        System.out.println("Calculator V2 с улучшениями запущен на порту 8080");
    }
}