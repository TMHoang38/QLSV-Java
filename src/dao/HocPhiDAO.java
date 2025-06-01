package dao;

import database.DBConnection;
import model.HocPhi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HocPhiDAO {
    private Connection conn;

    public HocPhiDAO() {
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể kết nối đến database: " + e.getMessage());
        }
    }

    public boolean themHocPhi(HocPhi hocPhi) {
        String sql = "INSERT INTO HocPhi(MaSV, HocKy, NamHoc, SoTienPhaiDong, SoTienDaDong, TrangThai, NgayDong) VALUES(?,?,?,?,?,?,?)";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, hocPhi.getMaSV());
                ps.setInt(2, hocPhi.getHocKy());
                ps.setString(3, hocPhi.getNamHoc());
                ps.setDouble(4, hocPhi.getSoTienPhaiDong());
                ps.setDouble(5, hocPhi.getSoTienDaDong());
                ps.setString(6, hocPhi.getTrangThai());
                ps.setDate(7, hocPhi.getNgayDong() != null ? new java.sql.Date(hocPhi.getNgayDong().getTime()) : null);
                
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
            throw new RuntimeException("Lỗi khi thêm học phí: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean suaHocPhi(HocPhi hocPhi) {
        String sql = "UPDATE HocPhi SET SoTienPhaiDong=?, SoTienDaDong=?, TrangThai=?, NgayDong=? WHERE MaHocPhi=?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, hocPhi.getSoTienPhaiDong());
                ps.setDouble(2, hocPhi.getSoTienDaDong());
                ps.setString(3, hocPhi.getTrangThai());
                ps.setDate(4, hocPhi.getNgayDong() != null ? new java.sql.Date(hocPhi.getNgayDong().getTime()) : null);
                ps.setInt(5, hocPhi.getMaHocPhi());
                
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
            throw new RuntimeException("Lỗi khi cập nhật học phí: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean xoaHocPhi(int maHocPhi) {
        String sql = "DELETE FROM HocPhi WHERE MaHocPhi=?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maHocPhi);
                
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
            throw new RuntimeException("Lỗi khi xóa học phí: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<HocPhi> layDanhSachHocPhiTheoMaSV(String maSV) {
        List<HocPhi> dsHocPhi = new ArrayList<>();
        String sql = "SELECT * FROM HocPhi WHERE MaSV=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HocPhi hocPhi = new HocPhi();
                    hocPhi.setMaHocPhi(rs.getInt("MaHocPhi"));
                    hocPhi.setMaSV(rs.getString("MaSV"));
                    hocPhi.setHocKy(rs.getInt("HocKy"));
                    hocPhi.setNamHoc(rs.getString("NamHoc"));
                    hocPhi.setSoTienPhaiDong(rs.getDouble("SoTienPhaiDong"));
                    hocPhi.setSoTienDaDong(rs.getDouble("SoTienDaDong"));
                    hocPhi.setTrangThai(rs.getString("TrangThai"));
                    hocPhi.setNgayDong(rs.getDate("NgayDong"));
                    dsHocPhi.add(hocPhi);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách học phí: " + e.getMessage());
        }
        return dsHocPhi;
    }

    public List<HocPhi> layDanhSachHocPhiTheoNamHoc(String namHoc) {
        List<HocPhi> dsHocPhi = new ArrayList<>();
        String sql = "SELECT * FROM HocPhi WHERE NamHoc=? ORDER BY MaSV, HocKy";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, namHoc);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HocPhi hocPhi = new HocPhi();
                    hocPhi.setMaHocPhi(rs.getInt("MaHocPhi"));
                    hocPhi.setMaSV(rs.getString("MaSV"));
                    hocPhi.setHocKy(rs.getInt("HocKy"));
                    hocPhi.setNamHoc(rs.getString("NamHoc"));
                    hocPhi.setSoTienPhaiDong(rs.getDouble("SoTienPhaiDong"));
                    hocPhi.setSoTienDaDong(rs.getDouble("SoTienDaDong"));
                    hocPhi.setTrangThai(rs.getString("TrangThai"));
                    hocPhi.setNgayDong(rs.getDate("NgayDong"));
                    dsHocPhi.add(hocPhi);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách học phí: " + e.getMessage());
        }
        return dsHocPhi;
    }

    public double tinhTongHocPhiPhaiDong(String maSV, String namHoc) {
        String sql = "SELECT SUM(SoTienPhaiDong) as TongHocPhi FROM HocPhi WHERE MaSV=? AND NamHoc=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, namHoc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("TongHocPhi");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tính tổng học phí: " + e.getMessage());
        }
        return 0.0;
    }

    public double tinhTongHocPhiDaDong(String maSV, String namHoc) {
        String sql = "SELECT SUM(SoTienDaDong) as TongDaDong FROM HocPhi WHERE MaSV=? AND NamHoc=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, namHoc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("TongDaDong");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tính tổng học phí đã đóng: " + e.getMessage());
        }
        return 0.0;
    }

    public List<HocPhi> layTatCaHocPhi() {
        List<HocPhi> dsHocPhi = new ArrayList<>();
        String sql = "SELECT * FROM HocPhi ORDER BY MaSV, NamHoc, HocKy";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                HocPhi hocPhi = new HocPhi();
                hocPhi.setMaHocPhi(rs.getInt("MaHocPhi"));
                hocPhi.setMaSV(rs.getString("MaSV"));
                hocPhi.setHocKy(rs.getInt("HocKy"));
                hocPhi.setNamHoc(rs.getString("NamHoc"));
                hocPhi.setSoTienPhaiDong(rs.getDouble("SoTienPhaiDong"));
                hocPhi.setSoTienDaDong(rs.getDouble("SoTienDaDong"));
                hocPhi.setTrangThai(rs.getString("TrangThai"));
                hocPhi.setNgayDong(rs.getDate("NgayDong"));
                dsHocPhi.add(hocPhi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách học phí: " + e.getMessage());
        }
        return dsHocPhi;
    }
} 