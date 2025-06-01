package model;

import java.util.Date;

public class HocPhi {
    private int maHocPhi;
    private String maSV;
    private int hocKy;
    private String namHoc;
    private double soTienPhaiDong;
    private double soTienDaDong;
    private String trangThai;
    private Date ngayDong;

    public HocPhi() {
        this.soTienDaDong = 0.0;
    }

    public HocPhi(int maHocPhi, String maSV, int hocKy, String namHoc, 
                  double soTienPhaiDong, double soTienDaDong, String trangThai, Date ngayDong) {
        this.maHocPhi = maHocPhi;
        this.maSV = maSV;
        this.hocKy = hocKy;
        this.namHoc = namHoc;
        this.soTienPhaiDong = soTienPhaiDong;
        this.soTienDaDong = soTienDaDong;
        this.trangThai = trangThai;
        this.ngayDong = ngayDong;
    }

    // Getters and Setters
    public int getMaHocPhi() {
        return maHocPhi;
    }

    public void setMaHocPhi(int maHocPhi) {
        this.maHocPhi = maHocPhi;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public int getHocKy() {
        return hocKy;
    }

    public void setHocKy(int hocKy) {
        this.hocKy = hocKy;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public double getSoTienPhaiDong() {
        return soTienPhaiDong;
    }

    public void setSoTienPhaiDong(double soTienPhaiDong) {
        this.soTienPhaiDong = soTienPhaiDong;
    }

    public double getSoTienDaDong() {
        return soTienDaDong;
    }

    public void setSoTienDaDong(double soTienDaDong) {
        this.soTienDaDong = soTienDaDong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Date getNgayDong() {
        return ngayDong;
    }

    public void setNgayDong(Date ngayDong) {
        this.ngayDong = ngayDong;
    }

    // Phương thức tiện ích để tính số tiền còn thiếu
    public double getSoTienConThieu() {
        return soTienPhaiDong - soTienDaDong;
    }
} 