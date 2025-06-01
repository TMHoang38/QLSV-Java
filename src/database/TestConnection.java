package database;

import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Đang thử kết nối đến database...");
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                System.out.println("Kết nối database thành công!");
            } else {
                System.out.println("Kết nối database thất bại!");
            }
        } catch (Exception e) {
            System.out.println("Lỗi kết nối database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DBConnection.closeConnection(conn);
            }
        }
    }
} 