package com.synapse.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionUtil {
    private static final String URL = "jdbc:postgresql://localhost:5433/synapse";
    private static final String USER = "postgres";
    private static final String PASS = "NewStrongPassword123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            System.out.println(" Connected to " + con.getMetaData().getDatabaseProductName() +
                               " " + con.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.out.println(" Connection failed: " + e.getMessage());
        }
    }
}
