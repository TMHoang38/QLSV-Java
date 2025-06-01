package util;

import dao.DiemDAO;
import dao.HocPhiDAO;
import dao.SinhVienDAO;
import model.Diem;
import model.HocPhi;
import model.SinhVien;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BaoCaoUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public static void xuatBangDiem(String maSV) throws IOException {
        SinhVienDAO sinhVienDAO = new SinhVienDAO();
        DiemDAO diemDAO = new DiemDAO();

        SinhVien sv = sinhVienDAO.timSinhVienTheoMa(maSV);
        if (sv == null) {
            throw new IOException("Không tìm thấy sinh viên với mã " + maSV);
        }

        String fileName = "BangDiem_" + maSV + "_" + System.currentTimeMillis() + ".txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            // Thông tin sinh viên
            writer.write("BẢNG ĐIỂM SINH VIÊN\n");
            writer.write("------------------\n\n");
            writer.write("Mã sinh viên: " + sv.getMaSV() + "\n");
            writer.write("Họ tên: " + sv.getHoTen() + "\n");
            writer.write("Ngày sinh: " + sv.getNgaySinh() + "\n");
            writer.write("Giới tính: " + sv.getGioiTinh() + "\n\n");

            // Bảng điểm
            writer.write("BẢNG ĐIỂM CHI TIẾT\n");
            writer.write("STT\tMã MH\tHọc kỳ\tNăm học\tĐiểm QT\tĐiểm thi\tĐiểm TK\n");
            writer.write("--------------------------------------------------------\n");

            List<Diem> dsDiem = diemDAO.layDanhSachDiem(maSV);
            int stt = 1;
            float tongDiem = 0;
            int soMon = 0;

            for (Diem diem : dsDiem) {
                writer.write(String.format("%d\t%s\t%d\t%s\t%.1f\t%.1f\t%.1f\n",
                    stt++,
                    diem.getMaMH(),
                    diem.getHocKy(),
                    diem.getNamHoc(),
                    diem.getDiemQuaTrinh(),
                    diem.getDiemThi(),
                    diem.getDiemTongKet()
                ));
                tongDiem += diem.getDiemTongKet();
                soMon++;
            }

            writer.write("--------------------------------------------------------\n");
            if (soMon > 0) {
                float diemTB = tongDiem / soMon;
                writer.write(String.format("\nĐiểm trung bình tích lũy: %.2f\n", diemTB));
            }

            writer.write("\nNgày xuất báo cáo: " + DATE_FORMAT.format(new Date()));
        }
    }

    public static void xuatDanhSachHocPhi(String namHoc) throws IOException {
        HocPhiDAO hocPhiDAO = new HocPhiDAO();
        SinhVienDAO sinhVienDAO = new SinhVienDAO();

        String fileName = "DanhSachHocPhi_" + namHoc + "_" + System.currentTimeMillis() + ".txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("DANH SÁCH HỌC PHÍ NĂM HỌC " + namHoc + "\n");
            writer.write("--------------------------------\n\n");

            writer.write("STT\tMã SV\tHọ tên\tHọc kỳ\tSố tiền phải đóng\tSố tiền đã đóng\tTrạng thái\tNgày đóng\n");
            writer.write("--------------------------------------------------------\n");

            List<HocPhi> dsHocPhi = hocPhiDAO.layDanhSachHocPhiTheoNamHoc(namHoc);
            int stt = 1;
            double tongTienPhaiDong = 0;
            double tongTienDaDong = 0;

            for (HocPhi hp : dsHocPhi) {
                SinhVien sv = sinhVienDAO.timSinhVienTheoMa(hp.getMaSV());
                if (sv != null) {
                    writer.write(String.format("%d\t%s\t%s\t%d\t%.0f\t%.0f\t%s\t%s\n",
                        stt++,
                        hp.getMaSV(),
                        sv.getHoTen(),
                        hp.getHocKy(),
                        hp.getSoTienPhaiDong(),
                        hp.getSoTienDaDong(),
                        hp.getTrangThai(),
                        hp.getNgayDong() != null ? DATE_FORMAT.format(hp.getNgayDong()) : ""
                    ));
                    tongTienPhaiDong += hp.getSoTienPhaiDong();
                    tongTienDaDong += hp.getSoTienDaDong();
                }
            }

            writer.write("--------------------------------------------------------\n");
            writer.write(String.format("\nTổng số tiền phải đóng: %.0f VNĐ\n", tongTienPhaiDong));
            writer.write(String.format("Tổng số tiền đã đóng: %.0f VNĐ\n", tongTienDaDong));
            writer.write(String.format("Tổng số tiền còn nợ: %.0f VNĐ\n", tongTienPhaiDong - tongTienDaDong));
            writer.write("\nNgày xuất báo cáo: " + DATE_FORMAT.format(new Date()));
        }
    }
} 