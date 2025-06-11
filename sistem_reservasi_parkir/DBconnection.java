import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/parkir_gedung";
    private static final String USER = "root";
    private static final String PASSWORD = "iqbalM_86"; // sesuaikan dengan MySQL kamu

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
