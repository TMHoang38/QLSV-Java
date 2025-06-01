package gui;

import dao.DiemDAO;
import model.Diem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

public class QuanLyDiemGUI extends JPanel {
    private JTable tblDiem;
    private DefaultTableModel modelDiem;
    private JTextField txtMaSV, txtMaMH, txtHocKy, txtNamHoc, txtDiemQuaTrinh, txtDiemThi, txtDiemTongKet;
    private JButton btnThem, btnSua, btnXoa, btnTimKiem, btnLamMoi;
    private DiemDAO diemDAO;

    public QuanLyDiemGUI() {
        diemDAO = new DiemDAO();
        initComponents();
        loadDanhSachDiem();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Panel nhập liệu
        JPanel pnlInput = new JPanel(new GridBagLayout());
        pnlInput.setBorder(BorderFactory.createTitledBorder("Thông tin điểm"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Khởi tạo các components
        txtMaSV = new JTextField(15);
        txtMaMH = new JTextField(15);
        txtHocKy = new JTextField(15);
        txtNamHoc = new JTextField(15);
        txtDiemQuaTrinh = new JTextField(15);
        txtDiemThi = new JTextField(15);
        txtDiemTongKet = new JTextField(15);
        txtDiemTongKet.setEditable(false); // Không cho phép sửa điểm tổng kết

        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnTimKiem = new JButton("Tìm kiếm");
        btnLamMoi = new JButton("Làm mới");

        // Thêm components vào panel
        gbc.gridx = 0; gbc.gridy = 0;
        pnlInput.add(new JLabel("Mã SV:"), gbc);
        gbc.gridx = 1;
        pnlInput.add(txtMaSV, gbc);

        gbc.gridx = 2;
        pnlInput.add(new JLabel("Mã MH:"), gbc);
        gbc.gridx = 3;
        pnlInput.add(txtMaMH, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        pnlInput.add(new JLabel("Học kỳ:"), gbc);
        gbc.gridx = 1;
        pnlInput.add(txtHocKy, gbc);

        gbc.gridx = 2;
        pnlInput.add(new JLabel("Năm học:"), gbc);
        gbc.gridx = 3;
        pnlInput.add(txtNamHoc, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        pnlInput.add(new JLabel("Điểm quá trình:"), gbc);
        gbc.gridx = 1;
        pnlInput.add(txtDiemQuaTrinh, gbc);

        gbc.gridx = 2;
        pnlInput.add(new JLabel("Điểm thi:"), gbc);
        gbc.gridx = 3;
        pnlInput.add(txtDiemThi, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        pnlInput.add(new JLabel("Điểm tổng kết:"), gbc);
        gbc.gridx = 1;
        pnlInput.add(txtDiemTongKet, gbc);

        // Panel chứa các nút
        JPanel pnlButtons = new JPanel(new FlowLayout());
        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnTimKiem);
        pnlButtons.add(btnLamMoi);

        // Tạo bảng điểm
        String[] columnNames = {"Mã SV", "Mã MH", "Học kỳ", "Năm học", "Điểm quá trình", "Điểm thi", "Điểm tổng kết"};
        modelDiem = new DefaultTableModel(columnNames, 0);
        tblDiem = new JTable(modelDiem);
        JScrollPane scrollPane = new JScrollPane(tblDiem);

        // Panel bên trái chứa input và buttons
        JPanel pnlLeft = new JPanel(new BorderLayout());
        pnlLeft.add(pnlInput, BorderLayout.CENTER);
        pnlLeft.add(pnlButtons, BorderLayout.SOUTH);

        // Layout chính
        add(pnlLeft, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);

        // Thêm sự kiện tự động tính điểm tổng kết
        txtDiemQuaTrinh.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                tinhDiemTongKet();
            }
        });

        txtDiemThi.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                tinhDiemTongKet();
            }
        });

        // Đăng ký sự kiện cho các nút
        btnThem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                themDiem();
            }
        });

        btnSua.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                suaDiem();
            }
        });

        btnXoa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xoaDiem();
            }
        });

        btnTimKiem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timKiemDiem();
            }
        });

        btnLamMoi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lamMoi();
            }
        });

        // Thêm sự kiện click cho bảng
        tblDiem.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tblDiem.getSelectedRow();
                if (row >= 0) {
                    // Lấy dữ liệu từ bảng và điền vào các ô input
                    txtMaSV.setText(tblDiem.getValueAt(row, 0).toString());
                    txtMaMH.setText(tblDiem.getValueAt(row, 1).toString());
                    txtHocKy.setText(tblDiem.getValueAt(row, 2).toString());
                    txtNamHoc.setText(tblDiem.getValueAt(row, 3).toString());
                    txtDiemQuaTrinh.setText(tblDiem.getValueAt(row, 4).toString());
                    txtDiemThi.setText(tblDiem.getValueAt(row, 5).toString());
                    txtDiemTongKet.setText(tblDiem.getValueAt(row, 6).toString());
                }
            }
        });
    }

    private void tinhDiemTongKet() {
        try {
            if (!txtDiemQuaTrinh.getText().isEmpty() && !txtDiemThi.getText().isEmpty()) {
                String diemQTStr = txtDiemQuaTrinh.getText().trim().replace(",", ".");
                String diemThiStr = txtDiemThi.getText().trim().replace(",", ".");
                
                float diemQuaTrinh = Float.parseFloat(diemQTStr);
                float diemThi = Float.parseFloat(diemThiStr);
                
                if (diemQuaTrinh >= 0 && diemQuaTrinh <= 10 && diemThi >= 0 && diemThi <= 10) {
                    float diemTongKet = (diemQuaTrinh * 0.4f) + (diemThi * 0.6f);
                    txtDiemTongKet.setText(String.format("%.1f", diemTongKet).replace(",", "."));
                }
            }
        } catch (NumberFormatException e) {
            // Không làm gì khi nhập sai định dạng
        }
    }

    private void hienThiThongTinDiem() {
        int selectedRow = tblDiem.getSelectedRow();
        if (selectedRow >= 0) {
            txtMaSV.setText(tblDiem.getValueAt(selectedRow, 0).toString());
            txtMaMH.setText(tblDiem.getValueAt(selectedRow, 1).toString());
            txtHocKy.setText(tblDiem.getValueAt(selectedRow, 2).toString());
            txtNamHoc.setText(tblDiem.getValueAt(selectedRow, 3).toString());
            txtDiemQuaTrinh.setText(tblDiem.getValueAt(selectedRow, 4).toString());
            txtDiemThi.setText(tblDiem.getValueAt(selectedRow, 5).toString());
            txtDiemTongKet.setText(tblDiem.getValueAt(selectedRow, 6).toString());
        }
    }

    private void lamMoi() {
        // Xóa nội dung các ô input
        txtMaSV.setText("");
        txtMaMH.setText("");
        txtHocKy.setText("");
        txtNamHoc.setText("");
        txtDiemQuaTrinh.setText("");
        txtDiemThi.setText("");
        txtDiemTongKet.setText("");

        // Xóa selection trong bảng
        tblDiem.clearSelection();
        
        // Load lại dữ liệu trong bảng
        loadDanhSachDiem();
        
        // Focus vào ô mã sinh viên
        txtMaSV.requestFocus();
    }

    private void loadDanhSachDiem() {
        modelDiem.setRowCount(0);
        List<Diem> dsDiem = diemDAO.layDanhSachDiem();
        for (Diem diem : dsDiem) {
            Object[] row = {
                diem.getMaSV(),
                diem.getMaMH(),
                diem.getHocKy(),
                diem.getNamHoc(),
                String.format("%.1f", diem.getDiemQuaTrinh()).replace(",", "."),
                String.format("%.1f", diem.getDiemThi()).replace(",", "."),
                String.format("%.1f", diem.getDiemTongKet()).replace(",", ".")
            };
            modelDiem.addRow(row);
        }
    }

    private void themDiem() {
        if (!validateInput()) {
            return;
        }

        try {
            Diem diem = new Diem();
            diem.setMaSV(txtMaSV.getText().trim());
            diem.setMaMH(txtMaMH.getText().trim());
            diem.setHocKy(Integer.parseInt(txtHocKy.getText().trim()));
            diem.setNamHoc(txtNamHoc.getText().trim());
            
            String diemQTStr = txtDiemQuaTrinh.getText().trim().replace(",", ".");
            String diemThiStr = txtDiemThi.getText().trim().replace(",", ".");
            float diemQuaTrinh = Float.parseFloat(diemQTStr);
            float diemThi = Float.parseFloat(diemThiStr);
            float diemTongKet = Float.parseFloat(txtDiemTongKet.getText().trim());

            diem.setDiemQuaTrinh(diemQuaTrinh);
            diem.setDiemThi(diemThi);
            diem.setDiemTongKet(diemTongKet);

            if (diemDAO.themDiem(diem)) {
                JOptionPane.showMessageDialog(this, "Thêm điểm thành công!");
                loadDanhSachDiem();
                lamMoi();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm điểm thất bại!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số!");
        }
    }

    private void suaDiem() {
        int selectedRow = tblDiem.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn điểm cần sửa!");
            return;
        }

        if (!validateInput()) {
            return;
        }

        try {
            Diem diem = new Diem();
            diem.setMaSV(txtMaSV.getText().trim());
            diem.setMaMH(txtMaMH.getText().trim());
            diem.setHocKy(Integer.parseInt(txtHocKy.getText().trim()));
            diem.setNamHoc(txtNamHoc.getText().trim());
            
            float diemQuaTrinh = Float.parseFloat(txtDiemQuaTrinh.getText().trim());
            float diemThi = Float.parseFloat(txtDiemThi.getText().trim());
            float diemTongKet = Float.parseFloat(txtDiemTongKet.getText().trim());

            diem.setDiemQuaTrinh(diemQuaTrinh);
            diem.setDiemThi(diemThi);
            diem.setDiemTongKet(diemTongKet);

            if (diemDAO.suaDiem(diem)) {
                JOptionPane.showMessageDialog(this, "Sửa điểm thành công!");
                loadDanhSachDiem();
                lamMoi();
            } else {
                JOptionPane.showMessageDialog(this, "Sửa điểm thất bại!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số!");
        }
    }

    private boolean validateInput() {
        // Kiểm tra các trường bắt buộc
        if (txtMaSV.getText().trim().isEmpty() || 
            txtMaMH.getText().trim().isEmpty() || 
            txtHocKy.getText().trim().isEmpty() ||
            txtNamHoc.getText().trim().isEmpty() ||
            txtDiemQuaTrinh.getText().trim().isEmpty() ||
            txtDiemThi.getText().trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return false;
        }

        try {
            // Kiểm tra học kỳ
            int hocKy = Integer.parseInt(txtHocKy.getText().trim());
            if (hocKy < 1 || hocKy > 8) {
                JOptionPane.showMessageDialog(this, "Học kỳ phải từ 1 đến 8!");
                return false;
            }

            // Kiểm tra điểm quá trình
            String diemQTStr = txtDiemQuaTrinh.getText().trim().replace(",", ".");
            float diemQuaTrinh = Float.parseFloat(diemQTStr);
            if (diemQuaTrinh < 0 || diemQuaTrinh > 10) {
                JOptionPane.showMessageDialog(this, "Điểm quá trình phải từ 0 đến 10!");
                return false;
            }

            // Kiểm tra điểm thi
            String diemThiStr = txtDiemThi.getText().trim().replace(",", ".");
            float diemThi = Float.parseFloat(diemThiStr);
            if (diemThi < 0 || diemThi > 10) {
                JOptionPane.showMessageDialog(this, "Điểm thi phải từ 0 đến 10!");
                return false;
            }

            // Kiểm tra định dạng năm học (yyyy-yyyy)
            String namHoc = txtNamHoc.getText().trim();
            if (!namHoc.matches("\\d{4}-\\d{4}")) {
                JOptionPane.showMessageDialog(this, "Năm học phải có định dạng yyyy-yyyy (VD: 2023-2024)!");
                return false;
            }

            String[] namHocParts = namHoc.split("-");
            int namBatDau = Integer.parseInt(namHocParts[0]);
            int namKetThuc = Integer.parseInt(namHocParts[1]);
            if (namKetThuc - namBatDau != 1) {
                JOptionPane.showMessageDialog(this, "Năm kết thúc phải lớn hơn năm bắt đầu 1 năm!");
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số!");
            return false;
        }

        return true;
    }

    private void xoaDiem() {
        int selectedRow = tblDiem.getSelectedRow();
        if (selectedRow >= 0) {
            String maSV = tblDiem.getValueAt(selectedRow, 0).toString();
            String maMH = tblDiem.getValueAt(selectedRow, 1).toString();
            int hocKy = Integer.parseInt(tblDiem.getValueAt(selectedRow, 2).toString());
            String namHoc = tblDiem.getValueAt(selectedRow, 3).toString();

            int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa điểm này?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (diemDAO.xoaDiem(maSV, maMH, hocKy, namHoc)) {
                    JOptionPane.showMessageDialog(this, "Xóa điểm thành công!");
                    loadDanhSachDiem();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa điểm thất bại!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn điểm cần xóa!");
        }
    }

    private void timKiemDiem() {
        String tuKhoa = JOptionPane.showInputDialog(this, "Nhập từ khóa tìm kiếm:");
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            tuKhoa = tuKhoa.trim();
            modelDiem.setRowCount(0);
            List<Diem> dsDiem = diemDAO.layDanhSachDiem();
            
            for (Diem diem : dsDiem) {
                boolean timThay = false;
                
                // Tìm theo mã sinh viên
                if (diem.getMaSV().toLowerCase().contains(tuKhoa.toLowerCase())) {
                    timThay = true;
                }
                // Tìm theo mã môn học
                else if (diem.getMaMH().toLowerCase().contains(tuKhoa.toLowerCase())) {
                    timThay = true;
                }
                // Tìm theo học kỳ
                else if (String.valueOf(diem.getHocKy()).equals(tuKhoa)) {
                    timThay = true;
                }
                // Tìm theo năm học
                else if (diem.getNamHoc().contains(tuKhoa)) {
                    timThay = true;
                }
                // Tìm theo điểm tổng kết
                else {
                    try {
                        float diemTimKiem = Float.parseFloat(tuKhoa.replace(",", "."));
                        if (Math.abs(diem.getDiemTongKet() - diemTimKiem) < 0.01) {
                            timThay = true;
                        }
                    } catch (NumberFormatException e) {
                        // Không làm gì nếu từ khóa không phải là số
                    }
                }

                if (timThay) {
                    Object[] row = {
                        diem.getMaSV(),
                        diem.getMaMH(),
                        diem.getHocKy(),
                        diem.getNamHoc(),
                        String.format("%.1f", diem.getDiemQuaTrinh()).replace(",", "."),
                        String.format("%.1f", diem.getDiemThi()).replace(",", "."),
                        String.format("%.1f", diem.getDiemTongKet()).replace(",", ".")
                    };
                    modelDiem.addRow(row);
                }
            }
            
            if (modelDiem.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào!");
                loadDanhSachDiem(); // Load lại toàn bộ danh sách nếu không tìm thấy
            }
        } else if (tuKhoa == null) {
            // Người dùng đã nhấn Cancel
            return;
        } else {
            // Từ khóa rỗng
            loadDanhSachDiem();
        }
    }

    private void clearFields() {
        txtMaSV.setText("");
        txtMaMH.setText("");
        txtHocKy.setText("");
        txtNamHoc.setText("");
        txtDiemQuaTrinh.setText("");
        txtDiemThi.setText("");
        txtDiemTongKet.setText("");
        txtMaSV.requestFocus();
    }
} 