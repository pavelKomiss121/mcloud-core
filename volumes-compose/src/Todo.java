
import java.sql.*;
import java.util.Scanner;

import java.sql.PreparedStatement;

class TodoApp {
  private static final String DB_URL = "jdbc:postgresql://localhost:5432/todoapp?user=postgres&password=password";

  public static void main(String[] args) throws Exception {
    // Ожидание запуска БД
    Thread.sleep(5000);

    try (Connection conn = DriverManager.getConnection(DB_URL)) {
      initDatabase(conn);

      Scanner scanner = new Scanner(System.in);
      while (true) {
        System.out.println("\n=== TODO List ===");
        System.out.println("1. Показать задачи");
        System.out.println("2. Добавить задачу");
        System.out.println("3. Отметить выполненной");
        System.out.println("4. Удалить задачу");
        System.out.println("0. Выход");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
          case 1 -> showTodos(conn);
          case 2 -> {
            System.out.print("Введите задачу: ");
            String task = scanner.nextLine();
            addTodo(conn, task);
          }
          case 3 -> {
            System.out.print("ID задачи: ");
            int id = scanner.nextInt();
            completeTodo(conn, id);
          }
          case 4 -> {
            System.out.print("ID задачи: ");
            int id = scanner.nextInt();
            deleteTodo(conn, id);
          }
          case 0 -> {
            System.out.println("До свидания!");
            return;
          }
        }
      }
    }
  }

  private static void initDatabase(Connection conn) throws SQLException {
    Statement stmt = conn.createStatement();
    stmt.execute("CREATE TABLE IF NOT EXISTS todos (id SERIAL PRIMARY KEY, task VARCHAR(255) NOT NULL, completed BOOLEAN DEFAULT FALSE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"); // CREATE TABLE IF NOT EXISTS
  }

  private static void showTodos(Connection conn) throws SQLException {
    ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM todos");
    while (rs.next()) {
      System.out.println(rs.getString("task"));
    }
  }

  private static void addTodo(Connection conn, String task) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement("INSERT INTO todos (task) VALUES (?)");
    stmt.setString(1, task);
    stmt.executeUpdate();
    System.out.println("Задача добавлена!");
  }

  private static void completeTodo(Connection conn, int id) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement("UPDATE todos SET completed = ? WHERE id = ?");
    stmt.setBoolean(1, true);
    stmt.setInt(2, id);
    int rows = stmt.executeUpdate();
    System.out.println(rows > 0 ? "Задача отмечена выполненной!" : "Задача не найдена!");
  }

  private static void deleteTodo(Connection conn, int id) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement("DELETE FROM todos WHERE id = ?");
    stmt.setInt(1, id);
    int rows = stmt.executeUpdate();
    System.out.println(rows > 0 ? "Задача удалена!" : "Задача не найдена!");
  }
}