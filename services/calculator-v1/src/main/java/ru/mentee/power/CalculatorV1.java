package ru.mentee.power;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class CalculatorV1 {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/calculate", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String[] params = query.split("&");
            int a = Integer.parseInt(params[0].split("=")[1]);
            int b = Integer.parseInt(params[1].split("=")[1]);
            String operator = params[2].split("=")[1];
            int result = 0;
            switch (operator) {
                case "add": result = a + b; break;
                case "sub": result = a - b; break;
                case "mul": result = a * b; break;
                case "div": result = a / b; break;
            }
            String hostname = System.getenv("HOSTNAME");
            if (hostname == null) hostname = "localhost";

            String response = "{\"result\":" + result + ",\"version\":\"v1\",\"pod\":\"" + hostname + "\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.createContext("/health", exchange -> {
            String response = "{\"status\":\"UP\",\"service\":\"calculator-v1\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.start();
        System.out.println("Calculator V1 запущен на порту 8080");
    }
}