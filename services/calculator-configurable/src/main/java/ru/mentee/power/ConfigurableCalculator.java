package ru.mentee.power;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConfigurableCalculator {
    private Properties config;
    private Connection dbConnection;

    public static void main(String[] args) throws IOException {
        new ConfigurableCalculator().start();
    }

    public void start() throws IOException {
        loadConfiguration();
        initDatabase();

        int port = Integer.parseInt(getConfig("SERVER_PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            try {
                String responce = String.format("""
                        
                        """)
            }
            // TODO: Вернуть информацию о конфигурации
            // Показать загруженные параметры (кроме паролей!)
        });

        server.createContext("/calculate", exchange -> {
            // TODO: Выполнить вычисление
            // Сохранить результат в БД если включено
        });

        server.createContext("/config", exchange -> {
            // TODO: Показать текущую конфигурацию
            // БЕЗ sensitive данных
        });

        server.createContext("/external", exchange -> {
            // TODO: Вызвать внешний API используя API_KEY из Secret
        });

        server.start();
        System.out.println("Configurable Calculator запущен на порту " + port);
    }

    private void loadConfiguration() {
        // TODO: Загрузить конфигурацию из:
        // 1. Переменных окружения
        // 2. Файла /app/config/application.properties
        // 3. Значений по умолчанию
    }

    private void initDatabase() {
        // TODO: Подключиться к БД используя:
        // DATABASE_HOST из ConfigMap
        // DATABASE_PASSWORD из Secret
    }

    private String getConfig(String key, String defaultValue) {
        // TODO: Получить значение конфигурации
        return "";
    }
}