package ru.mentee.power;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExternalApiClient {
  private final HttpClient client = HttpClient.newHttpClient();
  private final String apiKey;
  private final String apiUrl;

  public ExternalApiClient(String apiKey, String apiUrl) {
    this.apiKey = apiKey;
    this.apiUrl = apiUrl;
  }


  public String callExternalApi(String endpoint) {
    try {
      String fullUrl = apiUrl.endsWith("/") ? apiUrl + endpoint : apiUrl + "/" + endpoint;

      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(fullUrl))
          .header("Authorization", "Bearer " + apiKey)
          .header("Content-Type", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        System.out.println("API вызов успешен: " + response.statusCode());
        return response.body();
      } else {
        System.err.println("API ошибка: " + response.statusCode() + " - " + response.body());
        return "{\"error\":\"API call failed with status " + response.statusCode() + "\"}";
      }


    } catch (Exception e) {
      System.err.println("Ошибка вызова API: " + e.getMessage());
      return "{\"error\":\"API call failed: " + e.getMessage() + "\"}";
    }
  }

  public boolean validateApiKey() {
    try {
      String response = callExternalApi("validate");
      return response.contains("\"valid\":true") || response.contains("\"status\":\"ok\"") || response.contains("\"success\":true");
    } catch (Exception e) {
      System.err.println("Ошибка валидации API ключа: " + e.getMessage());
      return false;
    }
  }
}