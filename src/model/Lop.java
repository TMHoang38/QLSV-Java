package model;

public class Lop {
    private String maLop;
    private String tenLop;
    private String maNganh;
    private String nienKhoa;
    private int siSo;

    public Lop() {
    }

    public Lop(String maLop, String tenLop, String maNganh, String nienKhoa, int siSo) {
        this.maLop = maLop;
        this.tenLop = tenLop;
        this.maNganh = maNganh;
        this.nienKhoa = nienKhoa;
        this.siSo = siSo;
    }

    // Getters and Setters
    public String getMaLop() {
        return maLop;
    }

    public void setMaLop(String maLop) {
        this.maLop = maLop;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public void setMaNganh(String maNganh) {
        this.maNganh = maNganh;
    }

    public String getNienKhoa() {
        return nienKhoa;
    }

    public void setNienKhoa(String nienKhoa) {
        this.nienKhoa = nienKhoa;
    }

    public int getSiSo() {
        return siSo;
    }

    public void setSiSo(int siSo) {
        this.siSo = siSo;
    }
} 