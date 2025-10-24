package ru.mentee.power;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
  private Connection connection;

  public void connect(String host, String port, String dbName, String user, String password) throws SQLException {
    String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
    connection = DriverManager.getConnection(jdbcUrl, user, password);
    System.out.println("Connected to database");
  }

  public void createTables() throws SQLException {
    if (connection == null) {
      System.err.println("Нет подключения к БД, результат не сохранен");
      return;
    }

    String createTableSQL = """
        CREATE TABLE IF NOT EXISTS calculations (
            id SERIAL PRIMARY KEY,
            expression VARCHAR(255),
            result DOUBLE PRECISION,
            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;
    try (var statement = connection.prepareStatement(createTableSQL)) {
      statement.execute();
      System.out.println("Таблица calculations создана или уже существует");
    }

  }

  public void saveCalculation(String expression, double result) throws SQLException {
    if (connection == null) {
      System.err.println("Нет подключения к БД, результат не сохранен");
      return;
    }

    String sql = "INSERT INTO calculations (expression, result) VALUES (?, ?)";
    try (var statement = connection.prepareStatement(sql)) {
      statement.setString(1, expression);
      statement.setDouble(2, result);
      statement.executeUpdate();
      System.out.println("Результат сохранен в БД: " + expression + " = " + result);
    } catch (SQLException e) {
      System.err.println("Ошибка сохранения в БД: " + e.getMessage());
    }
  }

  public String getConnectionInfo() {
    if (connection == null) {
      return "Нет подключения к БД";
    }
    try {
      String url = connection.getMetaData().getURL();
      String user = connection.getMetaData().getUserName();
      String dbName = connection.getCatalog();

      return String.format("БД: %s, Пользователь: %s, Каталог: %s", url, user, dbName);
    } catch (SQLException e) {
      return "Ошибка получения информации о подключении: " + e.getMessage();
    }
  }
}