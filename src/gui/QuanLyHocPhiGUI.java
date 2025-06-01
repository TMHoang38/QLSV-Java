package gui;

import dao.HocPhiDAO;
import dao.SinhVienDAO;
import model.HocPhi;
import model.SinhVien;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class QuanLyHocPhiGUI extends JPanel {
    private JTextField txtMaSinhVien, txtHocKy, txtNamHoc, txtSoTienPhaiDong, txtSoTienDaDong, txtSoTienConThieu, txtMaLop, txtNgayDong;
    private JComboBox<String> cboTrangThai;
    private JTable tblHocPhi;
    private DefaultTableModel modelHocPhi;
    private HocPhiDAO hocPhiDAO;
    private SinhVienDAO sinhVienDAO;
    private JButton btnThem, btnSua, btnXoa, btnTimKiem, btnLamMoi;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public QuanLyHocPhiGUI() {
        hocPhiDAO = new HocPhiDAO();
        sinhVienDAO = new SinhVienDAO();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        initComponents();
        loadDanhSachHocPhi();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Panel chứa các trường nhập liệu
        JPanel pnlInput = new JPanel(new GridLayout(9, 2, 5, 5));
        pnlInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        pnlInput.add(new JLabel("Mã sinh viên:"));
        txtMaSinhVien = new JTextField();
        pnlInput.add(txtMaSinhVien);

        pnlInput.add(new JLabel("Mã lớp:"));
        txtMaLop = new JTextField();
        txtMaLop.setEditable(false);
        pnlInput.add(txtMaLop);

        pnlInput.add(new JLabel("Học kỳ:"));
        txtHocKy = new JTextField();
        pnlInput.add(txtHocKy);

        pnlInput.add(new JLabel("Năm học:"));
        txtNamHoc = new JTextField();
        pnlInput.add(txtNamHoc);

        pnlInput.add(new JLabel("Số tiền phải đóng:"));
        txtSoTienPhaiDong = new JTextField();
        pnlInput.add(txtSoTienPhaiDong);

        pnlInput.add(new JLabel("Số tiền đã đóng:"));
        txtSoTienDaDong = new JTextField();
        pnlInput.add(txtSoTienDaDong);

        pnlInput.add(new JLabel("Số tiền còn thiếu:"));
        txtSoTienConThieu = new JTextField();
        txtSoTienConThieu.setEditable(false);
        txtSoTienConThieu.setBackground(Color.LIGHT_GRAY);
        pnlInput.add(txtSoTienConThieu);

        pnlInput.add(new JLabel("Ngày đóng (dd/MM/yyyy):"));
        txtNgayDong = new JTextField();
        pnlInput.add(txtNgayDong);

        pnlInput.add(new JLabel("Trạng thái:"));
        cboTrangThai = new JComboBox<>();
        cboTrangThai.setEnabled(false);
        cboTrangThai.addItem("Chưa đóng");
        pnlInput.add(cboTrangThai);

        // Add document listener for automatic calculation
        txtSoTienPhaiDong.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSoTienConThieu(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSoTienConThieu(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSoTienConThieu(); }
        });

        txtSoTienDaDong.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSoTienConThieu(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSoTienConThieu(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSoTienConThieu(); }
        });

        // Panel chứa các nút chức năng
        JPanel pnlButtons = new JPanel(new FlowLayout());
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnTimKiem = new JButton("Tìm kiếm");
        btnLamMoi = new JButton("Làm mới");

        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnTimKiem);
        pnlButtons.add(btnLamMoi);

        // Tạo bảng học phí với cột mới
        String[] columnNames = {
            "Mã học phí", "Mã sinh viên", "Mã lớp", "Học kỳ", "Năm học", 
            "Số tiền phải đóng", "Số tiền đã đóng", "Trạng thái", "Ngày đóng"
        };
        modelHocPhi = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHocPhi = new JTable(modelHocPhi);
        JScrollPane scrollPane = new JScrollPane(tblHocPhi);

        // Layout chính
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlInput, BorderLayout.CENTER);
        pnlTop.add(pnlButtons, BorderLayout.SOUTH);

        add(pnlTop, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Đăng ký sự kiện
        btnThem.addActionListener(e -> themHocPhi());
        btnSua.addActionListener(e -> suaHocPhi());
        btnXoa.addActionListener(e -> xoaHocPhi());
        btnTimKiem.addActionListener(e -> timKiemHocPhi());
        btnLamMoi.addActionListener(e -> lamMoi());

        // Sự kiện khi click vào dòng trong bảng
        tblHocPhi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = tblHocPhi.getSelectedRow();
                if (selectedRow >= 0) {
                    String maSV = modelHocPhi.getValueAt(selectedRow, 1).toString();
                    String maLop = modelHocPhi.getValueAt(selectedRow, 2).toString();
                    String hocKy = modelHocPhi.getValueAt(selectedRow, 3).toString();
                    String namHoc = modelHocPhi.getValueAt(selectedRow, 4).toString();
                    String soTienPhaiDong = modelHocPhi.getValueAt(selectedRow, 5).toString();
                    String soTienDaDong = modelHocPhi.getValueAt(selectedRow, 6).toString();
                    String trangThai = modelHocPhi.getValueAt(selectedRow, 7).toString();
                    String ngayDong = modelHocPhi.getValueAt(selectedRow, 8) != null ? 
                                    modelHocPhi.getValueAt(selectedRow, 8).toString() : "";

                    txtMaSinhVien.setText(maSV);
                    txtMaLop.setText(maLop);
                    txtHocKy.setText(hocKy);
                    txtNamHoc.setText(namHoc);
                    txtSoTienPhaiDong.setText(soTienPhaiDong.replace(" đ", "").replace(",", ""));
                    txtSoTienDaDong.setText(soTienDaDong.replace(" đ", "").replace(",", ""));
                    txtNgayDong.setText(ngayDong);

                    // Cập nhật ComboBox
                    if (cboTrangThai.getItemCount() > 0) {
                        cboTrangThai.removeAllItems();
                    }
                    cboTrangThai.addItem(trangThai);

                    // Tính toán số tiền còn thiếu
                    try {
                        double soTienPhaiDongValue = parseCurrency(soTienPhaiDong);
                        double soTienDaDongValue = parseCurrency(soTienDaDong);
                        double soTienConThieu = soTienPhaiDongValue - soTienDaDongValue;
                        txtSoTienConThieu.setText(formatCurrency(soTienConThieu));
                        txtNgayDong.setEnabled(soTienDaDongValue > 0);
                    } catch (Exception ex) {
                        txtSoTienConThieu.setText("");
                    }
                }
            }
        });
    }

    private void updateSoTienConThieu() {
        try {
            double soTienPhaiDong = parseCurrency(txtSoTienPhaiDong.getText());
            double soTienDaDong = parseCurrency(txtSoTienDaDong.getText());
            double soTienConThieu = soTienPhaiDong - soTienDaDong;

            txtSoTienConThieu.setText(formatCurrency(soTienConThieu));

            // Xác định và cập nhật trạng thái
            String trangThai;
            if (soTienDaDong == 0) {
                trangThai = "Chưa đóng";
            } else if (soTienConThieu > 0) {
                trangThai = "Nợ [" + formatCurrency(soTienConThieu) + "]";
            } else {
                trangThai = "Đã đóng đủ";
            }

            // Cập nhật ComboBox
            if (cboTrangThai.getItemCount() > 0) {
                cboTrangThai.removeAllItems();
            }
            cboTrangThai.addItem(trangThai);

            // Xử lý ngày đóng
            txtNgayDong.setEnabled(soTienDaDong > 0);
            if (soTienDaDong > 0 && txtNgayDong.getText().trim().isEmpty()) {
                txtNgayDong.setText(dateFormat.format(new java.util.Date()));
            }
        } catch (NumberFormatException e) {
            txtSoTienConThieu.setText("");
            if (cboTrangThai.getItemCount() > 0) {
                cboTrangThai.removeAllItems();
            }
            cboTrangThai.addItem("Chưa đóng");
            txtNgayDong.setEnabled(false);
            txtNgayDong.setText("");
        }
    }

    private void loadDanhSachHocPhi() {
        modelHocPhi.setRowCount(0);
        List<HocPhi> dsHocPhi = hocPhiDAO.layTatCaHocPhi();
        SinhVienDAO sinhVienDAO = new SinhVienDAO();
        
        for (HocPhi hp : dsHocPhi) {
            SinhVien sv = sinhVienDAO.timSinhVienTheoMa(hp.getMaSV());
            String maLop = sv != null ? sv.getMaLop() : "";
            
            Object[] row = {
                hp.getMaHocPhi(),
                hp.getMaSV(),
                maLop, // Lấy mã lớp từ thông tin sinh viên
                hp.getHocKy(),
                hp.getNamHoc(),
                String.format("%,.0f đ", hp.getSoTienPhaiDong()),
                String.format("%,.0f đ", hp.getSoTienDaDong()),
                hp.getTrangThai(),
                hp.getNgayDong() != null ? dateFormat.format(hp.getNgayDong()) : ""
            };
            modelHocPhi.addRow(row);
        }
    }

    // Các phương thức xử lý sự kiện
    private void themHocPhi() {
        if (!validateInput()) {
            return;
        }

        try {
            HocPhi hocPhi = new HocPhi();
            hocPhi.setMaSV(txtMaSinhVien.getText().trim());
            hocPhi.setHocKy(Integer.parseInt(txtHocKy.getText().trim()));
            hocPhi.setNamHoc(txtNamHoc.getText().trim());
            hocPhi.setSoTienPhaiDong(parseCurrency(txtSoTienPhaiDong.getText()));
            double soTienDaDong = parseCurrency(txtSoTienDaDong.getText());
            hocPhi.setSoTienDaDong(soTienDaDong);
            
            String trangThai = cboTrangThai.getSelectedItem().toString();
            hocPhi.setTrangThai(trangThai);
            
            if (trangThai.equals("Đã đóng đủ")) {
                try {
                    java.util.Date ngayDong = dateFormat.parse(txtNgayDong.getText().trim());
                    hocPhi.setNgayDong(ngayDong);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Ngày đóng không hợp lệ! Vui lòng nhập theo định dạng dd/MM/yyyy");
                    return;
                }
            }
            
            if (hocPhiDAO.themHocPhi(hocPhi)) {
                JOptionPane.showMessageDialog(this, "Thêm học phí thành công!");
                loadDanhSachHocPhi();
                lamMoi();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm học phí thất bại!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void suaHocPhi() {
        int selectedRow = tblHocPhi.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn học phí cần sửa!");
            return;
        }

        if (!validateInput()) {
            return;
        }

        try {
            HocPhi hocPhi = new HocPhi();
            hocPhi.setMaHocPhi(Integer.parseInt(modelHocPhi.getValueAt(selectedRow, 0).toString()));
            hocPhi.setMaSV(txtMaSinhVien.getText().trim());
            hocPhi.setHocKy(Integer.parseInt(txtHocKy.getText().trim()));
            hocPhi.setNamHoc(txtNamHoc.getText().trim());
            
            double soTienPhaiDong = parseCurrency(txtSoTienPhaiDong.getText());
            double soTienDaDong = parseCurrency(txtSoTienDaDong.getText());
            
            hocPhi.setSoTienPhaiDong(soTienPhaiDong);
            hocPhi.setSoTienDaDong(soTienDaDong);

            // Xác định trạng thái dựa trên số tiền
            String trangThai;
            if (soTienDaDong == 0) {
                trangThai = "Chưa đóng";
                hocPhi.setNgayDong(null);
            } else if (soTienDaDong < soTienPhaiDong) {
                trangThai = "Nợ [" + formatCurrency(soTienPhaiDong - soTienDaDong) + "]";
                // Cập nhật ngày đóng nếu có
                String ngayDongStr = txtNgayDong.getText().trim();
                if (!ngayDongStr.isEmpty()) {
                    hocPhi.setNgayDong(dateFormat.parse(ngayDongStr));
                } else {
                    hocPhi.setNgayDong(new java.util.Date());
                }
            } else {
                trangThai = "Đã đóng đủ";
                // Cập nhật ngày đóng nếu có
                String ngayDongStr = txtNgayDong.getText().trim();
                if (!ngayDongStr.isEmpty()) {
                    hocPhi.setNgayDong(dateFormat.parse(ngayDongStr));
                } else {
                    hocPhi.setNgayDong(new java.util.Date());
                }
            }
            hocPhi.setTrangThai(trangThai);

            if (hocPhiDAO.suaHocPhi(hocPhi)) {
                JOptionPane.showMessageDialog(this, "Cập nhật học phí thành công!");
                loadDanhSachHocPhi();
                lamMoi();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật học phí thất bại!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void xoaHocPhi() {
        int selectedRow = tblHocPhi.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn học phí cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa học phí này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int maHocPhi = Integer.parseInt(modelHocPhi.getValueAt(selectedRow, 0).toString());
                if (hocPhiDAO.xoaHocPhi(maHocPhi)) {
                    JOptionPane.showMessageDialog(this, "Xóa học phí thành công!");
                    loadDanhSachHocPhi();
                    lamMoi();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa học phí thất bại!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
            }
        }
    }

    private void timKiemHocPhi() {
        String tuKhoa = JOptionPane.showInputDialog(this, "Nhập từ khóa tìm kiếm (Mã sinh viên, Mã lớp, Học kỳ, Năm học):");
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            tuKhoa = tuKhoa.trim().toLowerCase();
            List<HocPhi> dsHocPhi = hocPhiDAO.layTatCaHocPhi();
            modelHocPhi.setRowCount(0);
            SinhVienDAO sinhVienDAO = new SinhVienDAO();

            for (HocPhi hp : dsHocPhi) {
                SinhVien sv = sinhVienDAO.timSinhVienTheoMa(hp.getMaSV());
                String maLop = sv != null ? sv.getMaLop().toLowerCase() : "";
                
                // Chuyển các giá trị thành chuỗi để tìm kiếm
                String maHocPhi = String.valueOf(hp.getMaHocPhi()).toLowerCase();
                String maSV = hp.getMaSV().toLowerCase();
                String hocKy = String.valueOf(hp.getHocKy()).toLowerCase();
                String namHoc = hp.getNamHoc().toLowerCase();
                String soTienPhaiDong = currencyFormat.format(hp.getSoTienPhaiDong()).toLowerCase();
                String soTienDaDong = currencyFormat.format(hp.getSoTienDaDong()).toLowerCase();
                String trangThai = hp.getTrangThai().toLowerCase();
                String ngayDong = hp.getNgayDong() != null ? dateFormat.format(hp.getNgayDong()).toLowerCase() : "";

                // Kiểm tra từ khóa có tồn tại trong bất kỳ trường nào
                if (maHocPhi.contains(tuKhoa) ||
                    maSV.contains(tuKhoa) ||
                    maLop.contains(tuKhoa) ||
                    hocKy.contains(tuKhoa) ||
                    namHoc.contains(tuKhoa) ||
                    soTienPhaiDong.contains(tuKhoa) ||
                    soTienDaDong.contains(tuKhoa) ||
                    trangThai.contains(tuKhoa) ||
                    ngayDong.contains(tuKhoa)) {

                    Object[] row = {
                        hp.getMaHocPhi(),
                        hp.getMaSV(),
                        sv != null ? sv.getMaLop() : "",
                        hp.getHocKy(),
                        hp.getNamHoc(),
                        currencyFormat.format(hp.getSoTienPhaiDong()),
                        currencyFormat.format(hp.getSoTienDaDong()),
                        hp.getTrangThai(),
                        hp.getNgayDong() != null ? dateFormat.format(hp.getNgayDong()) : ""
                    };
                    modelHocPhi.addRow(row);
                }
            }

            if (modelHocPhi.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào!");
                loadDanhSachHocPhi(); // Load lại toàn bộ danh sách nếu không tìm thấy
            }
        } else if (tuKhoa == null) {
            // Người dùng đã nhấn Cancel
            return;
        } else {
            // Từ khóa rỗng
            loadDanhSachHocPhi();
        }
    }

    private void lamMoi() {
        txtMaSinhVien.setText("");
        txtMaLop.setText("");
        txtHocKy.setText("");
        txtNamHoc.setText("");
        txtSoTienPhaiDong.setText("");
        txtSoTienDaDong.setText("");
        txtSoTienConThieu.setText("");
        txtNgayDong.setText("");
        if (cboTrangThai.getItemCount() > 0) {
            cboTrangThai.removeAllItems();
        }
        cboTrangThai.addItem("Chưa đóng");
        txtNgayDong.setEnabled(false);
        tblHocPhi.clearSelection();
        loadDanhSachHocPhi();
    }

    // Phương thức public để refresh data từ bên ngoài
    public void refreshData() {
        loadDanhSachHocPhi();
    }

    private boolean validateInput() {
        // Kiểm tra các trường bắt buộc
        if (txtMaSinhVien.getText().isEmpty() || txtHocKy.getText().isEmpty() ||
            txtNamHoc.getText().isEmpty() || txtSoTienPhaiDong.getText().isEmpty() ||
            txtSoTienDaDong.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return false;
        }

        // Kiểm tra mã sinh viên tồn tại và lấy mã lớp
        SinhVien sv = sinhVienDAO.timSinhVienTheoMa(txtMaSinhVien.getText().trim());
        if (sv == null) {
            JOptionPane.showMessageDialog(this, "Mã sinh viên không tồn tại!");
            return false;
        } else {
            txtMaLop.setText(sv.getMaLop());
        }

        try {
            // Kiểm tra học kỳ
            int hocKy = Integer.parseInt(txtHocKy.getText().trim());
            if (hocKy < 1 || hocKy > 8) {
                JOptionPane.showMessageDialog(this, "Học kỳ phải từ 1 đến 8!");
                return false;
            }

            // Kiểm tra số tiền phải đóng và đã đóng
            double soTienPhaiDong = parseCurrency(txtSoTienPhaiDong.getText());
            double soTienDaDong = parseCurrency(txtSoTienDaDong.getText());
            
            if (soTienPhaiDong <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền phải đóng phải lớn hơn 0!");
                return false;
            }
            
            if (soTienDaDong < 0) {
                JOptionPane.showMessageDialog(this, "Số tiền đã đóng không được âm!");
                return false;
            }
            
            if (soTienDaDong > soTienPhaiDong) {
                JOptionPane.showMessageDialog(this, "Số tiền đã đóng không được lớn hơn số tiền phải đóng!");
                return false;
            }

            // Kiểm tra ngày đóng nếu đã đóng đủ
            String trangThai = cboTrangThai.getSelectedItem().toString();
            if (trangThai.equals("Đã đóng đủ")) {
                String ngayDongStr = txtNgayDong.getText().trim();
                if (ngayDongStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập ngày đóng khi đã đóng đủ tiền!");
                    return false;
                }
                try {
                    dateFormat.parse(ngayDongStr);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Ngày đóng không hợp lệ! Vui lòng nhập theo định dạng dd/MM/yyyy");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ cho học kỳ và số tiền!");
            return false;
        }

        return true;
    }

    private double parseCurrency(String amount) {
        try {
            return Double.parseDouble(amount.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String formatCurrency(double amount) {
        return currencyFormat.format(amount);
    }
} 