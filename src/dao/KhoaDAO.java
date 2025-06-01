package dao;

import database.DBConnection;
import model.Khoa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhoaDAO {
    private Connection conn;

    public KhoaDAO() {
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể kết nối đến database: " + e.getMessage());
        }
    }

    public boolean themKhoa(Khoa khoa) {
        String sql = "INSERT INTO Khoa(MaKhoa, TenKhoa, DiaChi, DienThoai, Email) VALUES(?,?,?,?,?)";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, khoa.getMaKhoa());
                ps.setString(2, khoa.getTenKhoa());
                ps.setString(3, khoa.getDiaChi());
                ps.setString(4, khoa.getDienThoai());
                ps.setString(5, khoa.getEmail());
                
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
            throw new RuntimeException("Lỗi khi thêm khoa: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean suaKhoa(Khoa khoa) {
        String sql = "UPDATE Khoa SET TenKhoa=?, DiaChi=?, DienThoai=?, Email=? WHERE MaKhoa=?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, khoa.getTenKhoa());
                ps.setString(2, khoa.getDiaChi());
                ps.setString(3, khoa.getDienThoai());
                ps.setString(4, khoa.getEmail());
                ps.setString(5, khoa.getMaKhoa());
                
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
            throw new RuntimeException("Lỗi khi cập nhật khoa: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean xoaKhoa(String maKhoa) {
        String sql = "DELETE FROM Khoa WHERE MaKhoa=?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maKhoa);
                
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
            throw new RuntimeException("Lỗi khi xóa khoa: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Khoa> layDanhSachKhoa() {
        List<Khoa> dsKhoa = new ArrayList<>();
        String sql = "SELECT * FROM Khoa ORDER BY MaKhoa";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Khoa khoa = new Khoa();
                khoa.setMaKhoa(rs.getString("MaKhoa"));
                khoa.setTenKhoa(rs.getString("TenKhoa"));
                khoa.setDiaChi(rs.getString("DiaChi"));
                khoa.setDienThoai(rs.getString("DienThoai"));
                khoa.setEmail(rs.getString("Email"));
                dsKhoa.add(khoa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách khoa: " + e.getMessage());
        }
        return dsKhoa;
    }

    public Khoa timKhoaTheoMa(String maKhoa) {
        String sql = "SELECT * FROM Khoa WHERE MaKhoa=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhoa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Khoa khoa = new Khoa();
                    khoa.setMaKhoa(rs.getString("MaKhoa"));
                    khoa.setTenKhoa(rs.getString("TenKhoa"));
                    khoa.setDiaChi(rs.getString("DiaChi"));
                    khoa.setDienThoai(rs.getString("DienThoai"));
                    khoa.setEmail(rs.getString("Email"));
                    return khoa;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tìm khoa: " + e.getMessage());
        }
        return null;
    }

    public List<Khoa> timKhoaTheoTen(String tenKhoa) {
        List<Khoa> dsKhoa = new ArrayList<>();
        String sql = "SELECT * FROM Khoa WHERE TenKhoa LIKE ? ORDER BY MaKhoa";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + tenKhoa + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Khoa khoa = new Khoa();
                    khoa.setMaKhoa(rs.getString("MaKhoa"));
                    khoa.setTenKhoa(rs.getString("TenKhoa"));
                    khoa.setDiaChi(rs.getString("DiaChi"));
                    khoa.setDienThoai(rs.getString("DienThoai"));
                    khoa.setEmail(rs.getString("Email"));
                    dsKhoa.add(khoa);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tìm khoa theo tên: " + e.getMessage());
        }
        return dsKhoa;
    }

    public boolean kiemTraTonTai(String maKhoa) {
        String sql = "SELECT COUNT(*) FROM Khoa WHERE MaKhoa=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhoa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi kiểm tra tồn tại khoa: " + e.getMessage());
        }
        return false;
    }

    public int demSoLuongSinhVien(String maKhoa) {
        String sql = "SELECT COUNT(*) FROM SinhVien sv " +
                    "JOIN Lop l ON sv.MaLop = l.MaLop " +
                    "WHERE l.MaKhoa = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhoa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi đếm số lượng sinh viên: " + e.getMessage());
        }
        return 0;
    }
} 