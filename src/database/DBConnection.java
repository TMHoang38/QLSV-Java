package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String SERVER_NAME = "LAPTOP-H95A2D2T\\SQLEXPRESS";
    private static final String DATABASE_NAME = "QLSV";
    private static final String DATABASE_PORT = "1433";
    private static final String DATABASE_USER = "sa";
    private static final String DATABASE_PASSWORD = "123";
    private static final String DATABASE_URL = String.format(
        "jdbc:sqlserver://%s:%s;databaseName=%s;trustServerCertificate=true",
        SERVER_NAME, DATABASE_PORT, DATABASE_NAME
    );

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: SQL Server JDBC Driver không tìm thấy.");
            System.err.println("Vui lòng thêm thư viện mssql-jdbc vào project.");
            e.printStackTrace();
        }
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            if (conn != null) {
                System.out.println("Đã tạo kết nối database mới.");
            }
            return conn;
        } catch (SQLException e) {
            System.err.println("Error: Không thể kết nối đến database.");
            System.err.println("URL: " + DATABASE_URL);
            System.err.println("User: " + DATABASE_USER);
            System.err.println("Chi tiết lỗi: " + e.getMessage());
            throw e;
        }
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Đã đóng kết nối database.");
            } catch (SQLException e) {
                System.err.println("Error: Không thể đóng kết nối database.");
                e.printStackTrace();
            }
        }
    }
} 