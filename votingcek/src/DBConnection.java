import java.sql.*;
import javax.swing.JOptionPane;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/college_voting";
    private static final String USER = "root";
    private static final String PASS = "ajay@2005";
    
    private static boolean driverLoaded = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            showError("MySQL JDBC Driver not found. Please include it in your library path!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            throw new SQLException("MySQL JDBC Driver not loaded");
        }

        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            showError("Database connection failed! Please check if MySQL server is running.", e);
            throw e;
        }
    }

    private static void showError(String message, Exception e) {
        String fullMessage = message + "\nError: " + e.getMessage();
        System.err.println(fullMessage);
        JOptionPane.showMessageDialog(null, fullMessage, "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
