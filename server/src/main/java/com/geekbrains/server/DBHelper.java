package com.geekbrains.server;

import java.sql.*;

public class DBHelper implements AutoCloseable {
    private static DBHelper instance;
    private static Connection connection;

    private DBHelper() {
    }

    public static DBHelper getInstance() {
        if (instance == null) {
            loadDriverAndOpenConnection();
            instance = new DBHelper();
        }
        return instance;
    }

    private static void loadDriverAndOpenConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:clientDB.db");

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка открытия соединения с базой данных!");
            e.printStackTrace();
        }
    }

    public String findByLoginAndPassword(String login, String password) {
        String query = String.format("SELECT name FROM client WHERE login=\"%s\" AND password=\"%s\"", login, password);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                return resultSet.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int updateNickname(String oldNickname, String newNickname) {
        String query = String.format("UPDATE client SET name=\"%s\" WHERE name=\"%s\"", newNickname, oldNickname);

        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(query);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}