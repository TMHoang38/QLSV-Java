package dao;

import database.DBConnection;
import model.Diem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiemDAO {
    private Connection conn;

    public DiemDAO() {
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể kết nối đến database: " + e.getMessage());
        }
    }

    public boolean themDiem(Diem diem) {
        String sql = "INSERT INTO Diem(MaSV, MaMH, HocKy, NamHoc, DiemQuaTrinh, DiemThi, DiemTongKet) VALUES(?,?,?,?,?,?,?)";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, diem.getMaSV());
                ps.setString(2, diem.getMaMH());
                ps.setInt(3, diem.getHocKy());
                ps.setString(4, diem.getNamHoc());
                ps.setFloat(5, diem.getDiemQuaTrinh());
                ps.setFloat(6, diem.getDiemThi());
                ps.setFloat(7, diem.getDiemTongKet());
                
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
            throw new RuntimeException("Lỗi khi thêm điểm: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean suaDiem(Diem diem) {
        String sql = "UPDATE Diem SET DiemQuaTrinh=?, DiemThi=?, DiemTongKet=? WHERE MaSV=? AND MaMH=? AND HocKy=? AND NamHoc=?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setFloat(1, diem.getDiemQuaTrinh());
                ps.setFloat(2, diem.getDiemThi());
                ps.setFloat(3, diem.getDiemTongKet());
                ps.setString(4, diem.getMaSV());
                ps.setString(5, diem.getMaMH());
                ps.setInt(6, diem.getHocKy());
                ps.setString(7, diem.getNamHoc());
                
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
            throw new RuntimeException("Lỗi khi cập nhật điểm: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean xoaDiem(String maSV, String maMH, int hocKy, String namHoc) {
        String sql = "DELETE FROM Diem WHERE MaSV=? AND MaMH=? AND HocKy=? AND NamHoc=?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maSV);
                ps.setString(2, maMH);
                ps.setInt(3, hocKy);
                ps.setString(4, namHoc);
                
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
            throw new RuntimeException("Lỗi khi xóa điểm: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Diem> layDanhSachDiem(String maSV) {
        List<Diem> dsDiem = new ArrayList<>();
        String sql = "SELECT * FROM Diem WHERE MaSV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Diem diem = new Diem();
                    diem.setMaSV(rs.getString("MaSV"));
                    diem.setMaMH(rs.getString("MaMH"));
                    diem.setHocKy(rs.getInt("HocKy"));
                    diem.setNamHoc(rs.getString("NamHoc"));
                    diem.setDiemQuaTrinh(rs.getFloat("DiemQuaTrinh"));
                    diem.setDiemThi(rs.getFloat("DiemThi"));
                    diem.setDiemTongKet(rs.getFloat("DiemTongKet"));
                    dsDiem.add(diem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách điểm: " + e.getMessage());
        }
        return dsDiem;
    }

    public List<Diem> layDanhSachDiem() {
        List<Diem> dsDiem = new ArrayList<>();
        String sql = "SELECT * FROM Diem ORDER BY MaSV, HocKy";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Diem diem = new Diem();
                diem.setMaSV(rs.getString("MaSV"));
                diem.setMaMH(rs.getString("MaMH"));
                diem.setHocKy(rs.getInt("HocKy"));
                diem.setNamHoc(rs.getString("NamHoc"));
                diem.setDiemQuaTrinh(rs.getFloat("DiemQuaTrinh"));
                diem.setDiemThi(rs.getFloat("DiemThi"));
                diem.setDiemTongKet(rs.getFloat("DiemTongKet"));
                dsDiem.add(diem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách điểm: " + e.getMessage());
        }
        return dsDiem;
    }

    public List<Diem> layDanhSachDiemTheoMaSV(String maSV) {
        List<Diem> dsDiem = new ArrayList<>();
        String sql = "SELECT * FROM Diem WHERE MaSV=? ORDER BY HocKy";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Diem diem = new Diem();
                    diem.setMaSV(rs.getString("MaSV"));
                    diem.setMaMH(rs.getString("MaMH"));
                    diem.setHocKy(rs.getInt("HocKy"));
                    diem.setNamHoc(rs.getString("NamHoc"));
                    diem.setDiemQuaTrinh(rs.getFloat("DiemQuaTrinh"));
                    diem.setDiemThi(rs.getFloat("DiemThi"));
                    diem.setDiemTongKet(rs.getFloat("DiemTongKet"));
                    dsDiem.add(diem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách điểm: " + e.getMessage());
        }
        return dsDiem;
    }

    public float tinhDiemTrungBinh(String maSV, int hocKy, String namHoc) {
        String sql = "SELECT AVG(DiemTongKet) as DTB FROM Diem WHERE MaSV=? AND HocKy=? AND NamHoc=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setInt(2, hocKy);
            ps.setString(3, namHoc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("DTB");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tính điểm trung bình: " + e.getMessage());
        }
        return 0.0f;
    }
} 