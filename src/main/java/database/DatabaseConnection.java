package database;

import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/fit_nutrition";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1234";

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            // Configurar propiedades de conexión
            Properties props = new Properties();
            props.setProperty("user", USERNAME);
            props.setProperty("password", PASSWORD);
            props.setProperty("useSSL", "false");
            props.setProperty("serverTimezone", "UTC");
            props.setProperty("allowPublicKeyRetrieval", "true");

            this.connection = DriverManager.getConnection(URL, props);
            System.out.println("Conexión a la base de datos establecida exitosamente");
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null || instance.connection == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar la conexión: " + e.getMessage());
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión cerrada exitosamente");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
