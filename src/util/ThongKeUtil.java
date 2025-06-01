package util;

import dao.DiemDAO;
import dao.HocPhiDAO;
import dao.SinhVienDAO;
import model.Diem;
import model.HocPhi;
import model.SinhVien;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ThongKeUtil {
    private static DiemDAO diemDAO = new DiemDAO();
    private static HocPhiDAO hocPhiDAO = new HocPhiDAO();

    public static void hienThiThongKeDiem() {
        // Tạo form nhập thông tin
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField txtMaHocPhan = new JTextField();
        JComboBox<Integer> cboHocKy = new JComboBox<>(new Integer[]{1, 2});
        JTextField txtNamHoc = new JTextField();
        
        panel.add(new JLabel("Mã học phần:"));
        panel.add(txtMaHocPhan);
        panel.add(new JLabel("Học kỳ:"));
        panel.add(cboHocKy);
        panel.add(new JLabel("Năm học:"));
        panel.add(txtNamHoc);

        int result = JOptionPane.showConfirmDialog(null, panel, "Thống kê điểm",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            final String maHocPhan = txtMaHocPhan.getText().trim();
            final Integer hocKy = (Integer) cboHocKy.getSelectedItem();
            final String namHoc = txtNamHoc.getText().trim();

            List<Diem> dsDiem = diemDAO.layDanhSachDiem();

            // Lọc theo điều kiện nếu có
            if (!maHocPhan.isEmpty() || !namHoc.isEmpty()) {
                final List<Diem> dsDiemLoc = dsDiem.stream()
                    .filter(diem -> {
                        boolean match = true;
                        if (!maHocPhan.isEmpty()) {
                            match = match && diem.getMaMH().equals(maHocPhan);
                        }
                        if (!namHoc.isEmpty()) {
                            match = match && diem.getNamHoc().equals(namHoc);
                        }
                        if (hocKy != null) {
                            match = match && diem.getHocKy() == hocKy;
                        }
                        return match;
                    })
                    .collect(Collectors.toList());
                dsDiem = dsDiemLoc;
            }

            if (dsDiem.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Không có dữ liệu thống kê cho điều kiện đã chọn!");
                return;
            }

            DefaultPieDataset dataset = new DefaultPieDataset();
            final List<Diem> dsDiemFinal = dsDiem;

            // Phân loại điểm chữ
            Map<String, Long> thongKeDiem = dsDiemFinal.stream()
                .collect(Collectors.groupingBy(diem -> {
                    float diemTK = diem.getDiemTongKet();
                    if (diemTK >= 8.5) return "A (8.5-10.0)";
                    else if (diemTK >= 7.0) return "B (7.0-8.4)";
                    else if (diemTK >= 5.5) return "C (5.5-6.9)";
                    else if (diemTK >= 4.0) return "D (4.0-5.4)";
                    else return "F (0.0-3.9)";
                }, Collectors.counting()));

            // Thêm dữ liệu vào dataset
            thongKeDiem.forEach((loai, soLuong) -> {
                double phanTram = (soLuong.doubleValue() / dsDiemFinal.size()) * 100;
                dataset.setValue(loai + " (" + String.format("%.1f%%", phanTram) + ")", phanTram);
            });

            // Tạo biểu đồ
            String title = "Thống kê phân bố điểm sinh viên";
            if (!maHocPhan.isEmpty()) title += " - Mã HP: " + maHocPhan;
            if (!namHoc.isEmpty()) title += " - " + namHoc;
            if (hocKy != null) title += " - Học kỳ " + hocKy;

            JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true,
                true,
                false
            );

            // Hiển thị biểu đồ trong cửa sổ mới
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(600, 400));

            JFrame frame = new JFrame("Thống kê điểm");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(chartPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }

    public static void hienThiThongKeHocPhi() {
        // Tạo form nhập thông tin
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField txtMaLop = new JTextField();
        JComboBox<Integer> cboHocKy = new JComboBox<>(new Integer[]{1, 2});
        JTextField txtNamHoc = new JTextField();
        
        panel.add(new JLabel("Mã lớp:"));
        panel.add(txtMaLop);
        panel.add(new JLabel("Học kỳ:"));
        panel.add(cboHocKy);
        panel.add(new JLabel("Năm học:"));
        panel.add(txtNamHoc);

        int result = JOptionPane.showConfirmDialog(null, panel, "Thống kê học phí",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            final String maLop = txtMaLop.getText().trim();
            final Integer hocKy = (Integer) cboHocKy.getSelectedItem();
            final String namHoc = txtNamHoc.getText().trim();

            List<HocPhi> dsHocPhi = hocPhiDAO.layTatCaHocPhi();
            SinhVienDAO sinhVienDAO = new SinhVienDAO();

            // Lọc theo điều kiện nếu có
            if (!maLop.isEmpty() || !namHoc.isEmpty()) {
                final List<HocPhi> dsHocPhiLoc = dsHocPhi.stream()
                    .filter(hp -> {
                        boolean match = true;
                        if (!maLop.isEmpty()) {
                            // Lấy sinh viên để kiểm tra mã lớp
                            SinhVien sv = sinhVienDAO.timSinhVienTheoMa(hp.getMaSV());
                            match = match && (sv != null && sv.getMaLop().equals(maLop));
                        }
                        if (!namHoc.isEmpty()) {
                            match = match && hp.getNamHoc().equals(namHoc);
                        }
                        if (hocKy != null) {
                            match = match && hp.getHocKy() == hocKy;
                        }
                        return match;
                    })
                    .collect(Collectors.toList());
                dsHocPhi = dsHocPhiLoc;
            }

            if (dsHocPhi.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Không có dữ liệu thống kê cho điều kiện đã chọn!");
                return;
            }

            DefaultPieDataset dataset = new DefaultPieDataset();
            final List<HocPhi> dsHocPhiFinal = dsHocPhi;

            // Đếm số lượng theo trạng thái
            long daDong = dsHocPhiFinal.stream()
                .filter(hp -> hp.getTrangThai().equals("Đã đóng đủ"))
                .count();
            
            long dangNo = dsHocPhiFinal.stream()
                .filter(hp -> hp.getTrangThai().startsWith("Nợ"))
                .count();
                
            long chuaDong = dsHocPhiFinal.stream()
                .filter(hp -> hp.getTrangThai().equals("Chưa đóng"))
                .count();

            // Tính phần trăm và thêm vào dataset
            double phanTramDaDong = (daDong * 100.0) / dsHocPhiFinal.size();
            double phanTramDangNo = (dangNo * 100.0) / dsHocPhiFinal.size();
            double phanTramChuaDong = (chuaDong * 100.0) / dsHocPhiFinal.size();

            dataset.setValue("Đã đóng (" + String.format("%.1f%%", phanTramDaDong) + ")", phanTramDaDong);
            dataset.setValue("Đang nợ (" + String.format("%.1f%%", phanTramDangNo) + ")", phanTramDangNo);
            dataset.setValue("Chưa đóng (" + String.format("%.1f%%", phanTramChuaDong) + ")", phanTramChuaDong);

            // Tạo biểu đồ
            String title = "Thống kê tình hình đóng học phí";
            if (!maLop.isEmpty()) title += " - Mã lớp: " + maLop;
            if (!namHoc.isEmpty()) title += " - " + namHoc;
            if (hocKy != null) title += " - Học kỳ " + hocKy;

            JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true,
                true,
                false
            );

            // Hiển thị biểu đồ trong cửa sổ mới
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(600, 400));

            JFrame frame = new JFrame("Thống kê học phí");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(chartPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }
} 