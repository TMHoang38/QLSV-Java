package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import dao.SinhVienDAO;
import model.SinhVien;
import util.BaoCaoUtil;
import util.ThongKeUtil;

public class MainGUI extends JFrame {
    private JTabbedPane tabbedPane;
    private QuanLySinhVienGUI sinhVienGUI;
    private QuanLyDiemGUI diemGUI;
    private QuanLyHocPhiGUI hocPhiGUI;
    private SinhVienDAO sinhVienDAO;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public MainGUI() {
        sinhVienDAO = new SinhVienDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("Quản lý sinh viên");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Tạo menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Menu Hệ thống
        JMenu menuHeThong = new JMenu("Hệ thống");
        JMenuItem menuItemThoat = new JMenuItem("Thoát");
        menuHeThong.add(menuItemThoat);
        menuBar.add(menuHeThong);

        // Menu Báo cáo
        JMenu menuBaoCao = new JMenu("Báo cáo");
        JMenuItem menuItemBangDiem = new JMenuItem("Bảng điểm");
        JMenuItem menuItemDanhSachHocPhi = new JMenuItem("Danh sách học phí");
        menuBaoCao.add(menuItemBangDiem);
        menuBaoCao.add(menuItemDanhSachHocPhi);
        menuBar.add(menuBaoCao);

        // Menu Tiện ích
        JMenu menuTienIch = new JMenu("Tiện ích");
        JMenuItem menuItemNhapSV = new JMenuItem("Nhập sinh viên từ file");
        JMenuItem menuItemThongKeDiem = new JMenuItem("Thống kê điểm");
        JMenuItem menuItemThongKeHocPhi = new JMenuItem("Thống kê học phí");
        menuTienIch.add(menuItemNhapSV);
        menuTienIch.addSeparator();
        menuTienIch.add(menuItemThongKeDiem);
        menuTienIch.add(menuItemThongKeHocPhi);
        menuBar.add(menuTienIch);

        // Tạo tabbed pane
        tabbedPane = new JTabbedPane();
        sinhVienGUI = new QuanLySinhVienGUI();
        diemGUI = new QuanLyDiemGUI();
        hocPhiGUI = new QuanLyHocPhiGUI();

        tabbedPane.addTab("Quản lý sinh viên", new JScrollPane(sinhVienGUI));
        tabbedPane.addTab("Quản lý điểm", new JScrollPane(diemGUI));
        tabbedPane.addTab("Quản lý học phí", new JScrollPane(hocPhiGUI));

        add(tabbedPane);

        // Đăng ký sự kiện
        menuItemThoat.addActionListener(e -> System.exit(0));
        menuItemBangDiem.addActionListener(e -> xuatBangDiem());
        menuItemDanhSachHocPhi.addActionListener(e -> xuatDanhSachHocPhi());
        menuItemNhapSV.addActionListener(e -> nhapSinhVienTuFile());
        menuItemThongKeDiem.addActionListener(e -> ThongKeUtil.hienThiThongKeDiem());
        menuItemThongKeHocPhi.addActionListener(e -> ThongKeUtil.hienThiThongKeHocPhi());
    }

    private void xuatBangDiem() {
        String maSV = JOptionPane.showInputDialog(this, "Nhập mã sinh viên cần xuất bảng điểm:");
        if (maSV != null && !maSV.isEmpty()) {
            try {
                BaoCaoUtil.xuatBangDiem(maSV);
                JOptionPane.showMessageDialog(this, "Đã xuất bảng điểm thành công!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất bảng điểm: " + e.getMessage());
            }
        }
    }

    private void xuatDanhSachHocPhi() {
        String namHoc = JOptionPane.showInputDialog(this, "Nhập năm học cần xuất danh sách học phí:");
        if (namHoc != null && !namHoc.isEmpty()) {
            try {
                BaoCaoUtil.xuatDanhSachHocPhi(namHoc);
                JOptionPane.showMessageDialog(this, "Đã xuất danh sách học phí thành công!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất danh sách học phí: " + e.getMessage());
            }
        }
    }

    private void nhapSinhVienTuFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".txt") || f.isDirectory();
            }

            public String getDescription() {
                return "Text Files (*.txt)";
            }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                    // Skip comments and empty lines
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                        continue;
                    }

                    String[] data = line.split("\\|");
                    if (data.length >= 12) { // Make sure we have all required fields
                        SinhVien sv = new SinhVien();
                        sv.setMaSV(data[0].trim());
                        sv.setHoTen(data[1].trim());
                        try {
                            sv.setNgaySinh(dateFormat.parse(data[2].trim()));
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày sinh ở dòng " + (count + 1));
                            continue;
                        }
                        sv.setGioiTinh(data[3].trim());
                        sv.setDiaChi(data[4].trim());
                        sv.setSoDienThoai(data[5].trim());
                        sv.setEmail(data[6].trim());
                        sv.setQueQuan(data[7].trim());
                        sv.setDanToc(data[8].trim());
                        sv.setTonGiao(data[9].trim());
                        sv.setMaLop(data[10].trim());
                        sv.setTrangThai(data[11].trim());

                        if (sinhVienDAO.themSinhVien(sv)) {
                            count++;
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Đã nhập thành công " + count + " sinh viên!");
                sinhVienGUI.refreshData(); // Refresh the table after import
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
} 