package dao;

import database.DBConnection;
import model.Lop;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LopDAO {
    private Connection conn;

    public LopDAO() {
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể kết nối đến database: " + e.getMessage());
        }
    }

    public boolean themLop(Lop lop) {
        String sql = "INSERT INTO Lop(MaLop, TenLop, MaNganh, NienKhoa, SiSo) VALUES(?,?,?,?,?)";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, lop.getMaLop());
                ps.setString(2, lop.getTenLop());
                ps.setString(3, lop.getMaNganh());
                ps.setString(4, lop.getNienKhoa());
                ps.setInt(5, lop.getSiSo());
                
                int result = ps.executeUpdate();
                if (result > 0) {
                    conn.commit();
                    return true;
                }
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi thêm lớp: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean suaLop(Lop lop) {
        String sql = "UPDATE Lop SET TenLop=?, MaNganh=?, NienKhoa=?, SiSo=? WHERE MaLop=?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, lop.getTenLop());
                ps.setString(2, lop.getMaNganh());
                ps.setString(3, lop.getNienKhoa());
                ps.setInt(4, lop.getSiSo());
                ps.setString(5, lop.getMaLop());
                
                int result = ps.executeUpdate();
                if (result > 0) {
                    conn.commit();
                    return true;
                }
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật lớp: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean xoaLop(String maLop) {
        String sql = "DELETE FROM Lop WHERE MaLop=?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maLop);
                
                int result = ps.executeUpdate();
                if (result > 0) {
                    conn.commit();
                    return true;
                }
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xóa lớp: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Lop> layDanhSachLop() {
        List<Lop> dsLop = new ArrayList<>();
        String sql = "SELECT * FROM Lop";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Lop lop = new Lop();
                lop.setMaLop(rs.getString("MaLop"));
                lop.setTenLop(rs.getString("TenLop"));
                lop.setMaNganh(rs.getString("MaNganh"));
                lop.setNienKhoa(rs.getString("NienKhoa"));
                lop.setSiSo(rs.getInt("SiSo"));
                dsLop.add(lop);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách lớp: " + e.getMessage());
        }
        return dsLop;
    }

    public Lop timLopTheoMa(String maLop) {
        String sql = "SELECT * FROM Lop WHERE MaLop=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Lop lop = new Lop();
                    lop.setMaLop(rs.getString("MaLop"));
                    lop.setTenLop(rs.getString("TenLop"));
                    lop.setMaNganh(rs.getString("MaNganh"));
                    lop.setNienKhoa(rs.getString("NienKhoa"));
                    lop.setSiSo(rs.getInt("SiSo"));
                    return lop;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tìm lớp: " + e.getMessage());
        }
        return null;
    }
} 