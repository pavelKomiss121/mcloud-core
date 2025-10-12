package ru.mentee.power;

import com.sun.net.httpserver.HttpServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
        String response = String.format("""
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body>
                <h1>Configurable Calculator</h1>
                <h2>Информация о конфигурации:</h2>
                <p><strong>Порт сервера:</strong> %s</p>
                <p><strong>База данных:</strong> %s:%s/%s</p>
                <p><strong>Уровень логирования:</strong> %s</p>
                <p><strong>Кэширование:</strong> %s</p>
                <p><strong>Статус БД:</strong> %s</p>
                <hr>
                <h3>Доступные эндпоинты:</h3>
                <ul>
                    <li><a href="/config">/config</a> - Показать полную конфигурацию</li>
                    <li><a href="/calculate?expr=2+2">/calculate?expr=2+2</a> - Вычислить выражение</li>
                    <li><a href="/external">/external</a> - Вызвать внешний API</li>
                </ul>
            </body>
            </html>
            """, getConfig("SERVER_PORT", "8080"), getConfig("DATABASE_HOST", "localhost"), getConfig("DATABASE_PORT", "5432"), getConfig("DATABASE_NAME", "calculator_db"), getConfig("LOG_LEVEL", "INFO"), getConfig("CACHE_ENABLED", "false"), dbConnection != null ? "Подключена" : "Не подключена");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
      } catch (Exception e) {
        String errorResponse = "Ошибка: " + e.getMessage();
        byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(500, errorBytes.length);
        exchange.getResponseBody().write(errorBytes);
      } finally {
        exchange.close();
      }

    });

    server.createContext("/calculate", exchange -> {
      try {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("expr=")) {
          String errorResponse = "Ошибка: Необходим параметр expr";
          byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(400, errorBytes.length);
          exchange.getResponseBody().write(errorBytes);
          return;
        }
        String expr = query.substring("expr=".length());
        double result = evaluateExpression(expr);

        boolean saved = false;
        if (dbConnection != null && "true".equals(getConfig("CACHE_ENABLED", "false"))) {
          saveCalculation(expr, result);
          saved = true;
        }
        String response = String.format("""
            <html>
            <head>
                <meta charset="UTF-8">
            </head>
            <body>
                <h2>Результат вычисления</h2>
                <p><strong>Выражение:</strong> %s</p>
                <p><strong>Результат:</strong> %.2f</p>
                <p><strong>Saved to DB:</strong> %s</p>
                <hr>
                <a href="/">Назад на главную</a>
            </body>
            </html>
            """, expr, result, saved ? "Yes" : "No");

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        exchange.close();
      }
    });

    server.createContext("/config", exchange -> {
      try {
        String response = String.format("""
            {
                "server_port": "%s",
                "database_host": "%s",
                "database_port": "%s",
                "database_name": "%s",
                "log_level": "%s",
                "cache_enabled": "%s",
                "database_connected": %s
            }
            """, getConfig("SERVER_PORT", "8080"), getConfig("DATABASE_HOST", "localhost"), getConfig("DATABASE_PORT", "5432"), getConfig("DATABASE_NAME", "calculator_db"), getConfig("LOG_LEVEL", "INFO"), getConfig("CACHE_ENABLED", "false"), dbConnection != null);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
      } catch (Exception e) {
        String errorResponse = "Ошибка: " + e.getMessage();
        byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(500, errorBytes.length);
        exchange.getResponseBody().write(errorBytes);
      } finally {
        exchange.close();
      }
    });

    server.createContext("/external", exchange -> {
      try {
        String apiKey = getConfig("API_KEY", "");
        if (apiKey == null) {
          String errorResponse = "API_KEY не найден";
          exchange.sendResponseHeaders(500, errorResponse.getBytes().length);
          exchange.getResponseBody().write(errorResponse.getBytes());
          return;
        }

        String response = String.format("""
            {
                "api_key_found": true,
                "api_key_length": %d,
                "message": "Внешний API вызов выполнен успешно"
            }
            """, apiKey.length());
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);

      } catch (Exception e) {
        e.printStackTrace();
        String errorResponse = "Ошибка: " + e.getMessage();
        byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(500, errorBytes.length);
        exchange.getResponseBody().write(errorBytes);
      } finally {
        exchange.close();
      }
    });

    server.start();
    System.out.println("Configurable Calculator запущен на порту " + port);
  }

  private void saveCalculation(String expr, double result) {
    try {
      String sql = "INSERT INTO calculations (expression, result) VALUES (?, ?)";
      try (var statement = dbConnection.prepareStatement(sql)) {
        statement.setString(1, expr);
        statement.setDouble(2, result);
        statement.executeUpdate();
      }

    } catch (SQLException e) {
      System.err.println("Ошибка сохранения в БД: " + e.getMessage());
    }
  }


  private double evaluateExpression(String expr) {
    try {
      if (!expr.matches("[0-9+\\-*/().\\s]+")) {
        throw new IllegalArgumentException("Недопустимые символы в выражении");
      }
      expr = expr.replaceAll("\\s+", "");
      return evaluateSimple(expr);
    } catch (Exception e) {
      throw new IllegalArgumentException("Ошибка вычисления: " + e.getMessage());
    }
  }

  private double evaluateSimple(String expr) {
    String[] parts = expr.split("[+\\-*/]");
    String[] operators = expr.split("[0-9.]+");

    if (parts.length == 0) {
      return Double.parseDouble(expr);
    }

    double result = Double.parseDouble(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      if (i < operators.length) {
        String operator = operators[i];
        double operand = Double.parseDouble(parts[i]);

        switch (operator) {
          case "+":
            result = result + operand;
            break;
          case "-":
            result = result - operand;
            break;
          case "*":
            result = result * operand;
            break;
          case "/":
            result = result / operand;
            break;
        }
      }
    }
    return result;
  }


  private void loadConfiguration() {
    Properties props = new Properties();
    try (FileInputStream fis = new FileInputStream("/app/config/application.properties")) {
      props.load(fis);
      System.out.println("Конфигурация загружена из файла");
    } catch (IOException e) {
      System.out.println("Файл конфигурации не найден, используются значения по умолчанию");
    }
    this.config = props;
  }

  private void initDatabase() {
    try {
      String host = getConfig("DATABASE_HOST", "localhost");
      String port = getConfig("DATABASE_PORT", "5432");
      String user = getConfig("DATABASE_USER", "calculator_user");
      String password = getConfig("DATABASE_PASSWORD", "");
      String database = getConfig("DATABASE_NAME", "calculator_db");

      if (database == null || host == null || port == null || user == null || password == null) {
        System.out.println("Не все занчения для бд прееданы");
        return;
      }
      String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
      System.out.println("Подключение к БД установлено: " + host + ":" + port + "/" + database);

      dbConnection = DriverManager.getConnection(jdbcUrl, user, password);
    } catch (SQLException e) {
      System.err.println("Ошибка подключения к БД: " + e.getMessage());
      dbConnection = null;
    }
  }

  private String getConfig(String key, String defaultValue) {
    String env = System.getenv(key);
    if (env != null && !env.trim().isEmpty()) {
      return env;
    }

    if (config != null) {
      String prop = config.getProperty(key);  // ← Исправлено!
      if (prop != null && !prop.trim().isEmpty()) {
        return prop;
      }
    }

    return defaultValue;
  }
}