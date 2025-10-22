import java.sql.*;

public class DBConnection {
    static final String URL = "jdbc:mysql://localhost:3306/college_voting";
    static final String USER = "root";
    static final String PASS = "ajay@2005"; // Your MySQL password

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
