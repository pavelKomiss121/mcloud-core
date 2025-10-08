package ru.mentee.power;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExternalApiClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private String apiKey;
    private String apiUrl;

    public ExternalApiClient(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    public String callExternalApi(String endpoint) {
        // TODO: Вызвать внешний API
        // Добавить API key в заголовок Authorization
        // Обработать ответ
        return "";
    }

    public boolean validateApiKey() {
        // TODO: Проверить валидность API ключа
        // Вызвать /validate endpoint
        return false;
    }
}