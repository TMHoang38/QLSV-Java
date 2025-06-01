package dao;

import model.SinhVien;
import database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SinhVienDAO {
    
    // Thêm sinh viên mới
    public boolean themSinhVien(SinhVien sv) {
        String sql = "INSERT INTO SinhVien (MaSV, HoTen, NgaySinh, GioiTinh, DiaChi, SoDienThoai, " +
                    "Email, QueQuan, DanToc, TonGiao, MaLop, TrangThai) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        String sqlCheck = "SELECT COUNT(*) FROM SinhVien WHERE MaSV=?";
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Kiểm tra mã sinh viên đã tồn tại
            try (PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {
                checkStmt.setString(1, sv.getMaSV());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Mã sinh viên đã tồn tại");
                }
            }
            
            // Thêm sinh viên mới
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, sv.getMaSV());
                pstmt.setString(2, sv.getHoTen());
                pstmt.setDate(3, new java.sql.Date(sv.getNgaySinh().getTime()));
                pstmt.setString(4, sv.getGioiTinh());
                pstmt.setString(5, sv.getDiaChi());
                pstmt.setString(6, sv.getSoDienThoai());
                pstmt.setString(7, sv.getEmail());
                pstmt.setString(8, sv.getQueQuan());
                pstmt.setString(9, sv.getDanToc());
                pstmt.setString(10, sv.getTonGiao());
                pstmt.setString(11, sv.getMaLop());
                pstmt.setString(12, sv.getTrangThai());
                
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    conn.commit();
                    return true;
                }
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi thêm sinh viên: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // Cập nhật thông tin sinh viên
    public boolean capNhatSinhVien(SinhVien sv) {
        String sql = "UPDATE SinhVien SET HoTen=?, NgaySinh=?, GioiTinh=?, DiaChi=?, " +
                    "SoDienThoai=?, Email=?, QueQuan=?, DanToc=?, TonGiao=?, MaLop=?, TrangThai=? " +
                    "WHERE MaSV=?";
        
        String sqlCheck = "SELECT COUNT(*) FROM SinhVien WHERE MaSV=?";
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Kiểm tra sinh viên tồn tại
            try (PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {
                checkStmt.setString(1, sv.getMaSV());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new SQLException("Sinh viên không tồn tại");
                }
            }
            
            // Cập nhật sinh viên
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, sv.getHoTen());
                pstmt.setDate(2, new java.sql.Date(sv.getNgaySinh().getTime()));
                pstmt.setString(3, sv.getGioiTinh());
                pstmt.setString(4, sv.getDiaChi());
                pstmt.setString(5, sv.getSoDienThoai());
                pstmt.setString(6, sv.getEmail());
                pstmt.setString(7, sv.getQueQuan());
                pstmt.setString(8, sv.getDanToc());
                pstmt.setString(9, sv.getTonGiao());
                pstmt.setString(10, sv.getMaLop());
                pstmt.setString(11, sv.getTrangThai());
                pstmt.setString(12, sv.getMaSV());
                
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    conn.commit();
                    return true;
                }
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật sinh viên: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // Xóa sinh viên
    public boolean xoaSinhVien(String maSV) {
        // Kiểm tra và xóa các bản ghi liên quan trong bảng Diem và HocPhi trước
        String sqlDeleteDiem = "DELETE FROM Diem WHERE MaSV=?";
        String sqlDeleteHocPhi = "DELETE FROM HocPhi WHERE MaSV=?";
        String sqlDeleteSinhVien = "DELETE FROM SinhVien WHERE MaSV=?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Xóa điểm
            try (PreparedStatement pstmtDiem = conn.prepareStatement(sqlDeleteDiem)) {
                pstmtDiem.setString(1, maSV);
                pstmtDiem.executeUpdate();
            }
            
            // Xóa học phí
            try (PreparedStatement pstmtHocPhi = conn.prepareStatement(sqlDeleteHocPhi)) {
                pstmtHocPhi.setString(1, maSV);
                pstmtHocPhi.executeUpdate();
            }
            
            // Xóa sinh viên
            try (PreparedStatement pstmtSinhVien = conn.prepareStatement(sqlDeleteSinhVien)) {
                pstmtSinhVien.setString(1, maSV);
                int result = pstmtSinhVien.executeUpdate();
                if (result > 0) {
                    conn.commit();
                    return true;
                }
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Lỗi khi xóa sinh viên: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Tìm sinh viên theo mã
    public SinhVien timSinhVienTheoMa(String maSV) {
        String sql = "SELECT * FROM SinhVien WHERE MaSV=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, maSV);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new SinhVien(
                        rs.getString("MaSV"),
                        rs.getString("HoTen"),
                        rs.getDate("NgaySinh"),
                        rs.getString("GioiTinh"),
                        rs.getString("DiaChi"),
                        rs.getString("SoDienThoai"),
                        rs.getString("Email"),
                        rs.getString("QueQuan"),
                        rs.getString("DanToc"),
                        rs.getString("TonGiao"),
                        rs.getString("MaLop"),
                        rs.getString("TrangThai")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tìm sinh viên: " + e.getMessage());
        }
        return null;
    }
    
    // Lấy danh sách tất cả sinh viên
    public List<SinhVien> layDanhSachSinhVien() {
        List<SinhVien> dsSinhVien = new ArrayList<>();
        String sql = "SELECT * FROM SinhVien";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                SinhVien sv = new SinhVien(
                    rs.getString("MaSV"),
                    rs.getString("HoTen"),
                    rs.getDate("NgaySinh"),
                    rs.getString("GioiTinh"),
                    rs.getString("DiaChi"),
                    rs.getString("SoDienThoai"),
                    rs.getString("Email"),
                    rs.getString("QueQuan"),
                    rs.getString("DanToc"),
                    rs.getString("TonGiao"),
                    rs.getString("MaLop"),
                    rs.getString("TrangThai")
                );
                dsSinhVien.add(sv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách sinh viên: " + e.getMessage());
        }
        return dsSinhVien;
    }
    
    // Tìm kiếm sinh viên theo nhiều tiêu chí
    public List<SinhVien> timKiemSinhVien(String keyword) {
        List<SinhVien> ketQua = new ArrayList<>();
        String sql = "SELECT * FROM SinhVien WHERE MaSV LIKE ? OR HoTen LIKE ? OR MaLop LIKE ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SinhVien sv = new SinhVien(
                        rs.getString("MaSV"),
                        rs.getString("HoTen"),
                        rs.getDate("NgaySinh"),
                        rs.getString("GioiTinh"),
                        rs.getString("DiaChi"),
                        rs.getString("SoDienThoai"),
                        rs.getString("Email"),
                        rs.getString("QueQuan"),
                        rs.getString("DanToc"),
                        rs.getString("TonGiao"),
                        rs.getString("MaLop"),
                        rs.getString("TrangThai")
                    );
                    ketQua.add(sv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tìm kiếm sinh viên: " + e.getMessage());
        }
        return ketQua;
    }
    
    // Cập nhật trạng thái sinh viên
    public boolean capNhatTrangThaiSinhVien(String maSV, String trangThaiMoi) {
        String sql = "UPDATE SinhVien SET TrangThai=? WHERE MaSV=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, trangThaiMoi);
            pstmt.setString(2, maSV);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 