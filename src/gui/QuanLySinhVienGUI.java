package gui;

import dao.SinhVienDAO;
import model.SinhVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.toedter.calendar.JDateChooser;

public class QuanLySinhVienGUI extends JPanel {
    private JTable tableSinhVien;
    private DefaultTableModel tableModel;
    private JTextField txtMaSV, txtHoTen, txtNgaySinh, txtDiaChi, txtSDT, txtEmail;
    private JTextField txtQueQuan, txtDanToc, txtTonGiao, txtMaLop;
    private JComboBox<String> cboGioiTinh, cboTrangThai;
    private JButton btnThem, btnSua, btnXoa, btnTimKiem, btnLamMoi;
    private SinhVienDAO sinhVienDAO;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public QuanLySinhVienGUI() {
        sinhVienDAO = new SinhVienDAO();
        setupGUI();
        loadDanhSachSinhVien();
    }

    private void setupGUI() {
        // Bỏ các thiết lập của JFrame
        // setTitle("Quản Lý Sinh Viên");
        // setSize(1200, 700);
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setLocationRelativeTo(null);

        // Panel chứa form nhập liệu
        JPanel inputPanel = new JPanel(new GridLayout(6, 4, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Thêm các components vào inputPanel
        inputPanel.add(new JLabel("Mã SV:"));
        txtMaSV = new JTextField();
        inputPanel.add(txtMaSV);

        inputPanel.add(new JLabel("Họ tên:"));
        txtHoTen = new JTextField();
        inputPanel.add(txtHoTen);

        inputPanel.add(new JLabel("Ngày sinh (dd/MM/yyyy):"));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        txtNgaySinh = new JTextField(15);
        txtNgaySinh.setEditable(false);
        JButton btnDate = new JButton("...");
        btnDate.setPreferredSize(new Dimension(30, 20));
        datePanel.add(txtNgaySinh);
        datePanel.add(btnDate);
        inputPanel.add(datePanel);

        inputPanel.add(new JLabel("Giới tính:"));
        cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
        inputPanel.add(cboGioiTinh);

        inputPanel.add(new JLabel("Địa chỉ:"));
        txtDiaChi = new JTextField();
        inputPanel.add(txtDiaChi);

        inputPanel.add(new JLabel("Số điện thoại:"));
        txtSDT = new JTextField();
        inputPanel.add(txtSDT);

        inputPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        inputPanel.add(txtEmail);

        inputPanel.add(new JLabel("Quê quán:"));
        txtQueQuan = new JTextField();
        inputPanel.add(txtQueQuan);

        inputPanel.add(new JLabel("Dân tộc:"));
        txtDanToc = new JTextField();
        inputPanel.add(txtDanToc);

        inputPanel.add(new JLabel("Tôn giáo:"));
        txtTonGiao = new JTextField();
        inputPanel.add(txtTonGiao);

        inputPanel.add(new JLabel("Mã lớp:"));
        txtMaLop = new JTextField();
        inputPanel.add(txtMaLop);

        inputPanel.add(new JLabel("Trạng thái:"));
        cboTrangThai = new JComboBox<>(new String[]{
            "Đang học", "Bảo lưu", "Nghỉ học", "Tốt nghiệp", "Đình chỉ"
        });
        inputPanel.add(cboTrangThai);

        // Panel chứa các nút chức năng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnTimKiem = new JButton("Tìm kiếm");
        btnLamMoi = new JButton("Làm mới");

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnXoa);
        buttonPanel.add(btnTimKiem);
        buttonPanel.add(btnLamMoi);

        // Tạo bảng hiển thị danh sách sinh viên
        String[] columnNames = {
            "Mã SV", "Họ tên", "Ngày sinh", "Giới tính", "Địa chỉ", 
            "SĐT", "Email", "Quê quán", "Dân tộc", "Tôn giáo", 
            "Mã lớp", "Trạng thái"
        };
        tableModel = new DefaultTableModel(columnNames, 0);
        tableSinhVien = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableSinhVien);

        // Layout chính
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Thêm sự kiện cho các nút
        btnThem.addActionListener(e -> themSinhVien());
        btnSua.addActionListener(e -> suaSinhVien());
        btnXoa.addActionListener(e -> xoaSinhVien());
        btnTimKiem.addActionListener(e -> timKiemSinhVien());
        btnLamMoi.addActionListener(e -> lamMoiForm());

        // Thêm sự kiện chọn dòng trong bảng
        tableSinhVien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableSinhVien.getSelectedRow();
                if (selectedRow >= 0) {
                    hienThiThongTinSinhVien(selectedRow);
                }
            }
        });

        // Thêm xử lý sự kiện cho nút chọn ngày
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        btnDate.addActionListener(e -> {
            // Tạo một JDialog mới mỗi lần hiển thị
            JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Chọn ngày", true);
            dialog.setLayout(new BorderLayout());
            
            // Tạo một JDateChooser mới mỗi lần hiển thị
            JDateChooser tempChooser = new JDateChooser();
            tempChooser.setDateFormatString("dd/MM/yyyy");
            // Nếu đã có ngày được chọn trước đó, set lại
            try {
                if (!txtNgaySinh.getText().trim().isEmpty()) {
                    tempChooser.setDate(dateFormat.parse(txtNgaySinh.getText().trim()));
                }
            } catch (ParseException ex) {
                // Bỏ qua nếu không parse được ngày
            }
            
            JPanel chooserPanel = new JPanel(new FlowLayout());
            chooserPanel.add(tempChooser);
            dialog.add(chooserPanel, BorderLayout.CENTER);
            
            JPanel dateButtonPanel = new JPanel(new FlowLayout());
            JButton btnConfirm = new JButton("Xác nhận");
            btnConfirm.addActionListener(event -> {
                if (tempChooser.getDate() != null) {
                    txtNgaySinh.setText(dateFormat.format(tempChooser.getDate()));
                }
                dialog.dispose();
            });
            dateButtonPanel.add(btnConfirm);
            dialog.add(dateButtonPanel, BorderLayout.SOUTH);
            
            dialog.pack();
            dialog.setLocationRelativeTo(btnDate);
            dialog.setVisible(true);
        });
    }

    private void loadDanhSachSinhVien() {
        tableModel.setRowCount(0);
        List<SinhVien> dsSinhVien = sinhVienDAO.layDanhSachSinhVien();
        for (SinhVien sv : dsSinhVien) {
            Object[] row = {
                sv.getMaSV(), sv.getHoTen(), dateFormat.format(sv.getNgaySinh()),
                sv.getGioiTinh(), sv.getDiaChi(), sv.getSoDienThoai(),
                sv.getEmail(), sv.getQueQuan(), sv.getDanToc(),
                sv.getTonGiao(), sv.getMaLop(), sv.getTrangThai()
            };
            tableModel.addRow(row);
        }
    }

    private void hienThiThongTinSinhVien(int row) {
        txtMaSV.setText(tableModel.getValueAt(row, 0).toString());
        txtHoTen.setText(tableModel.getValueAt(row, 1).toString());
        txtNgaySinh.setText(tableModel.getValueAt(row, 2).toString());
        cboGioiTinh.setSelectedItem(tableModel.getValueAt(row, 3).toString());
        txtDiaChi.setText(tableModel.getValueAt(row, 4).toString());
        txtSDT.setText(tableModel.getValueAt(row, 5).toString());
        txtEmail.setText(tableModel.getValueAt(row, 6).toString());
        txtQueQuan.setText(tableModel.getValueAt(row, 7).toString());
        txtDanToc.setText(tableModel.getValueAt(row, 8).toString());
        txtTonGiao.setText(tableModel.getValueAt(row, 9).toString());
        txtMaLop.setText(tableModel.getValueAt(row, 10).toString());
        cboTrangThai.setSelectedItem(tableModel.getValueAt(row, 11).toString());
    }

    private void themSinhVien() {
        try {
            if (!validateForm()) {
                return;
            }
            SinhVien sv = layThongTinTuForm();
            if (sv == null) return;
            
            // Kiểm tra mã sinh viên đã tồn tại
            if (sinhVienDAO.timSinhVienTheoMa(sv.getMaSV()) != null) {
                JOptionPane.showMessageDialog(this, "Mã sinh viên đã tồn tại!");
                return;
            }
            
            if (sinhVienDAO.themSinhVien(sv)) {
                JOptionPane.showMessageDialog(this, "Thêm sinh viên thành công!");
                loadDanhSachSinhVien();
                lamMoiForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm sinh viên thất bại!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void suaSinhVien() {
        try {
            if (!validateForm()) {
                return;
            }
            String maSV = txtMaSV.getText().trim();
            if (sinhVienDAO.timSinhVienTheoMa(maSV) == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên cần sửa!");
                return;
            }
            
            SinhVien sv = layThongTinTuForm();
            if (sv == null) return;
            
            if (sinhVienDAO.capNhatSinhVien(sv)) {
                JOptionPane.showMessageDialog(this, "Cập nhật sinh viên thành công!");
                loadDanhSachSinhVien();
                lamMoiForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật sinh viên thất bại!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void xoaSinhVien() {
        String maSV = txtMaSV.getText().trim();
        if (maSV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên cần xóa!");
            return;
        }

        if (sinhVienDAO.timSinhVienTheoMa(maSV) == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa sinh viên này?", "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (sinhVienDAO.xoaSinhVien(maSV)) {
                    JOptionPane.showMessageDialog(this, "Xóa sinh viên thành công!");
                    loadDanhSachSinhVien();
                    lamMoiForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa sinh viên thất bại!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa sinh viên: " + e.getMessage());
            }
        }
    }

    private void timKiemSinhVien() {
        String tuKhoa = JOptionPane.showInputDialog(this, "Nhập từ khóa tìm kiếm:");
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            tuKhoa = tuKhoa.trim().toLowerCase();
            tableModel.setRowCount(0);
            List<SinhVien> dsSinhVien = sinhVienDAO.layDanhSachSinhVien();
            
            for (SinhVien sv : dsSinhVien) {
                // Kiểm tra từng trường thông tin
                boolean timThay = false;
                
                // Chuyển đổi tất cả dữ liệu sang chữ thường để so sánh
                if (sv.getMaSV().toLowerCase().contains(tuKhoa) ||
                    sv.getHoTen().toLowerCase().contains(tuKhoa) ||
                    dateFormat.format(sv.getNgaySinh()).toLowerCase().contains(tuKhoa) ||
                    sv.getGioiTinh().toLowerCase().contains(tuKhoa) ||
                    sv.getDiaChi().toLowerCase().contains(tuKhoa) ||
                    sv.getSoDienThoai().toLowerCase().contains(tuKhoa) ||
                    sv.getEmail().toLowerCase().contains(tuKhoa) ||
                    sv.getQueQuan().toLowerCase().contains(tuKhoa) ||
                    sv.getDanToc().toLowerCase().contains(tuKhoa) ||
                    sv.getTonGiao().toLowerCase().contains(tuKhoa) ||
                    sv.getMaLop().toLowerCase().contains(tuKhoa) ||
                    sv.getTrangThai().toLowerCase().contains(tuKhoa)) {
                    
                    timThay = true;
                }

                if (timThay) {
                    Object[] row = {
                        sv.getMaSV(),
                        sv.getHoTen(),
                        dateFormat.format(sv.getNgaySinh()),
                        sv.getGioiTinh(),
                        sv.getDiaChi(),
                        sv.getSoDienThoai(),
                        sv.getEmail(),
                        sv.getQueQuan(),
                        sv.getDanToc(),
                        sv.getTonGiao(),
                        sv.getMaLop(),
                        sv.getTrangThai()
                    };
                    tableModel.addRow(row);
                }
            }
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào!");
                loadDanhSachSinhVien(); // Load lại toàn bộ danh sách nếu không tìm thấy
            }
        } else if (tuKhoa == null) {
            // Người dùng đã nhấn Cancel
            return;
        } else {
            // Từ khóa rỗng
            loadDanhSachSinhVien();
        }
    }

    private void lamMoiForm() {
        txtMaSV.setText("");
        txtHoTen.setText("");
        txtNgaySinh.setText("");
        cboGioiTinh.setSelectedIndex(0);
        txtDiaChi.setText("");
        txtSDT.setText("");
        txtEmail.setText("");
        txtQueQuan.setText("");
        txtDanToc.setText("");
        txtTonGiao.setText("");
        txtMaLop.setText("");
        cboTrangThai.setSelectedIndex(0);
        tableSinhVien.clearSelection();
        loadDanhSachSinhVien();
    }

    private boolean validateForm() {
        if (txtMaSV.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã sinh viên không được để trống!");
            return false;
        }
        if (txtHoTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên không được để trống!");
            return false;
        }
        if (txtNgaySinh.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không được để trống!");
            return false;
        }
        try {
            dateFormat.parse(txtNgaySinh.getText().trim());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng dd/MM/yyyy!");
            return false;
        }
        if (txtMaLop.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã lớp không được để trống!");
            return false;
        }
        return true;
    }

    private SinhVien layThongTinTuForm() throws ParseException {
        String maSV = txtMaSV.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        Date ngaySinh = dateFormat.parse(txtNgaySinh.getText().trim());
        String gioiTinh = (String) cboGioiTinh.getSelectedItem();
        String diaChi = txtDiaChi.getText().trim();
        String sdt = txtSDT.getText().trim();
        String email = txtEmail.getText().trim();
        String queQuan = txtQueQuan.getText().trim();
        String danToc = txtDanToc.getText().trim();
        String tonGiao = txtTonGiao.getText().trim();
        String maLop = txtMaLop.getText().trim();
        String trangThai = (String) cboTrangThai.getSelectedItem();

        return new SinhVien(maSV, hoTen, ngaySinh, gioiTinh, diaChi,
                           sdt, email, queQuan, danToc, tonGiao,
                           maLop, trangThai);
    }

    // Thêm phương thức public để cho phép truy cập từ bên ngoài
    public void refreshData() {
        loadDanhSachSinhVien();
    }
} 