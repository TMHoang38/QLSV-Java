-- Tạo cơ sở dữ liệu
CREATE DATABASE QLSV
GO

USE QLSV
GO

-- Bảng Khoa
CREATE TABLE Khoa (
    MaKhoa VARCHAR(10) PRIMARY KEY,
    TenKhoa NVARCHAR(100) NOT NULL,
    NgayThanhLap DATE,
    MoTa NVARCHAR(500)
)
GO

-- Bảng Ngành học
CREATE TABLE NganhHoc (
    MaNganh VARCHAR(10) PRIMARY KEY,
    TenNganh NVARCHAR(100) NOT NULL,
    MaKhoa VARCHAR(10) FOREIGN KEY REFERENCES Khoa(MaKhoa),
    MoTa NVARCHAR(500)
)
GO

-- Bảng Lớp
CREATE TABLE Lop (
    MaLop VARCHAR(10) PRIMARY KEY,
    TenLop NVARCHAR(50) NOT NULL,
    MaNganh VARCHAR(10) FOREIGN KEY REFERENCES NganhHoc(MaNganh),
    NienKhoa VARCHAR(20),
    SiSo INT
)
GO

-- Bảng Sinh viên
CREATE TABLE SinhVien (
    MaSV VARCHAR(20) PRIMARY KEY,
    HoTen NVARCHAR(100) NOT NULL,
    NgaySinh DATE,
    GioiTinh NVARCHAR(10),
    DiaChi NVARCHAR(200),
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100),
    QueQuan NVARCHAR(100),
    DanToc NVARCHAR(50),
    TonGiao NVARCHAR(50),
    MaLop VARCHAR(10) FOREIGN KEY REFERENCES Lop(MaLop),
    TrangThai NVARCHAR(50) CHECK (TrangThai IN (N'Đang học', N'Bảo lưu', N'Nghỉ học', N'Tốt nghiệp', N'Đình chỉ'))
)
GO

-- Bảng Môn học
CREATE TABLE MonHoc (
    MaMH VARCHAR(10) PRIMARY KEY,
    TenMH NVARCHAR(100) NOT NULL,
    SoTinChi INT,
    MoTa NVARCHAR(500)
)
GO

-- Bảng Điểm
CREATE TABLE Diem (
    MaSV VARCHAR(20),
    MaMH VARCHAR(10),
    HocKy INT,
    NamHoc VARCHAR(20),
    DiemQuaTrinh FLOAT,
    DiemThi FLOAT,
    DiemTongKet FLOAT,
    PRIMARY KEY (MaSV, MaMH, HocKy, NamHoc),
    FOREIGN KEY (MaSV) REFERENCES SinhVien(MaSV),
    FOREIGN KEY (MaMH) REFERENCES MonHoc(MaMH)
)
GO

-- Bảng Học phí
CREATE TABLE HocPhi (
    MaHocPhi INT IDENTITY(1,1) PRIMARY KEY,
    MaSV VARCHAR(20) FOREIGN KEY REFERENCES SinhVien(MaSV),
    HocKy INT,
    NamHoc VARCHAR(20),
    SoTien DECIMAL(18,2),
    SoTienDong DECIMAL(18,2) DEFAULT 0 CHECK (SoTienDong >= 0),
    TrangThai NVARCHAR(50) CHECK (TrangThai IN (N'Đã đóng', N'Chưa đóng', N'Nợ')),
    NgayDong DATE
)
GO

-- Thêm ràng buộc kiểm tra SoTienDong không được lớn hơn SoTien
ALTER TABLE HocPhi ADD CONSTRAINT CHK_SoTienDong 
CHECK (SoTienDong <= SoTien)
GO

-- 1. Stored Procedure nhập sinh viên từ file txt
CREATE PROCEDURE sp_NhapSinhVienTuFile
    @MaSV VARCHAR(20),
    @HoTen NVARCHAR(100),
    @NgaySinh DATE,
    @GioiTinh NVARCHAR(10),
    @DiaChi NVARCHAR(200),
    @SoDienThoai VARCHAR(15),
    @Email VARCHAR(100),
    @QueQuan NVARCHAR(100),
    @DanToc NVARCHAR(50),
    @TonGiao NVARCHAR(50),
    @MaLop VARCHAR(10)
AS
BEGIN
    BEGIN TRY
        IF NOT EXISTS (SELECT 1 FROM Lop WHERE MaLop = @MaLop)
        BEGIN
            RAISERROR(N'Mã lớp không tồn tại!', 16, 1)
            RETURN
        END

        INSERT INTO SinhVien (MaSV, HoTen, NgaySinh, GioiTinh, DiaChi, SoDienThoai, 
                            Email, QueQuan, DanToc, TonGiao, MaLop, TrangThai)
        VALUES (@MaSV, @HoTen, @NgaySinh, @GioiTinh, @DiaChi, @SoDienThoai,
                @Email, @QueQuan, @DanToc, @TonGiao, @MaLop, N'Đang học')
    END TRY
    BEGIN CATCH
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE()
        RAISERROR(@ErrorMessage, 16, 1)
    END CATCH
END
GO

-- 2. Stored Procedure xuất danh sách sinh viên ra file
CREATE PROCEDURE sp_XuatDanhSachSinhVien
    @MaLop VARCHAR(10) = NULL,
    @MaKhoa VARCHAR(10) = NULL
AS
BEGIN
    SELECT 
        sv.MaSV,
        sv.HoTen,
        sv.NgaySinh,
        sv.GioiTinh,
        sv.DiaChi,
        sv.SoDienThoai,
        sv.Email,
        l.TenLop,
        k.TenKhoa,
        sv.TrangThai
    FROM SinhVien sv
    JOIN Lop l ON sv.MaLop = l.MaLop
    JOIN NganhHoc nh ON l.MaNganh = nh.MaNganh
    JOIN Khoa k ON nh.MaKhoa = k.MaKhoa
    WHERE (@MaLop IS NULL OR sv.MaLop = @MaLop)
    AND (@MaKhoa IS NULL OR k.MaKhoa = @MaKhoa)
END
GO

-- 3. Stored Procedure cập nhật điểm tổng kết
CREATE PROCEDURE sp_CapNhatDiemTongKet
    @MaSV VARCHAR(20),
    @MaMH VARCHAR(10),
    @HocKy INT,
    @NamHoc VARCHAR(20),
    @DiemQuaTrinh FLOAT,
    @DiemThi FLOAT
AS
BEGIN
    BEGIN TRY
        -- Kiểm tra sinh viên và môn học tồn tại
        IF NOT EXISTS (SELECT 1 FROM SinhVien WHERE MaSV = @MaSV)
        BEGIN
            RAISERROR(N'Sinh viên không tồn tại!', 16, 1)
            RETURN
        END

        IF NOT EXISTS (SELECT 1 FROM MonHoc WHERE MaMH = @MaMH)
        BEGIN
            RAISERROR(N'Môn học không tồn tại!', 16, 1)
            RETURN
        END

        -- Tính điểm tổng kết (40% quá trình + 60% thi)
        DECLARE @DiemTongKet FLOAT = (@DiemQuaTrinh * 0.4) + (@DiemThi * 0.6)

        -- Cập nhật hoặc thêm mới điểm
        IF EXISTS (SELECT 1 FROM Diem WHERE MaSV = @MaSV AND MaMH = @MaMH AND HocKy = @HocKy AND NamHoc = @NamHoc)
        BEGIN
            UPDATE Diem
            SET DiemQuaTrinh = @DiemQuaTrinh,
                DiemThi = @DiemThi,
                DiemTongKet = @DiemTongKet
            WHERE MaSV = @MaSV AND MaMH = @MaMH AND HocKy = @HocKy AND NamHoc = @NamHoc
        END
        ELSE
        BEGIN
            INSERT INTO Diem (MaSV, MaMH, HocKy, NamHoc, DiemQuaTrinh, DiemThi, DiemTongKet)
            VALUES (@MaSV, @MaMH, @HocKy, @NamHoc, @DiemQuaTrinh, @DiemThi, @DiemTongKet)
        END
    END TRY
    BEGIN CATCH
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE()
        RAISERROR(@ErrorMessage, 16, 1)
    END CATCH
END
GO

-- 4. Function tính học phí còn nợ của sinh viên
CREATE FUNCTION fn_TinhHocPhiNo
(
    @MaSV VARCHAR(20)
)
RETURNS DECIMAL(18,2)
AS
BEGIN
    DECLARE @TongNo DECIMAL(18,2)

    SELECT @TongNo = ISNULL(SUM(SoTien), 0)
    FROM HocPhi
    WHERE MaSV = @MaSV AND TrangThai = N'Nợ'

    RETURN @TongNo
END
GO

-- 5. Trigger kiểm tra và cập nhật sĩ số lớp khi thêm/xóa sinh viên
CREATE TRIGGER trg_CapNhatSiSoLop
ON SinhVien
AFTER INSERT, DELETE
AS
BEGIN
    -- Cập nhật sĩ số cho lớp có sinh viên được thêm vào
    UPDATE l
    SET SiSo = (
        SELECT COUNT(*)
        FROM SinhVien sv
        WHERE sv.MaLop = l.MaLop
    )
    FROM Lop l
    WHERE l.MaLop IN (
        SELECT MaLop FROM inserted
        UNION
        SELECT MaLop FROM deleted
    )
END
GO

-- 6. Trigger kiểm tra điểm nhập vào hợp lệ
GO
CREATE TRIGGER trg_KiemTraDiem
ON Diem
AFTER INSERT, UPDATE
AS
BEGIN
    IF EXISTS (
        SELECT 1
        FROM inserted
        WHERE DiemQuaTrinh < 0 OR DiemQuaTrinh > 10
        OR DiemThi < 0 OR DiemThi > 10
        OR DiemTongKet < 0 OR DiemTongKet > 10
    )
    BEGIN
        RAISERROR(N'Điểm phải nằm trong khoảng từ 0 đến 10!', 16, 1)
        ROLLBACK TRANSACTION
        RETURN
    END
END
GO

-- 7. Stored Procedure thống kê kết quả học tập theo lớp
go
CREATE PROCEDURE sp_ThongKeKetQuaHocTapTheoLop
    @MaLop VARCHAR(10),
    @HocKy INT,
    @NamHoc VARCHAR(20)
AS
BEGIN
    SELECT 
        l.MaLop,
        l.TenLop,
        COUNT(DISTINCT sv.MaSV) as TongSoSV,
        COUNT(DISTINCT CASE WHEN d.DiemTongKet >= 5 THEN sv.MaSV END) as SoSVDat,
        COUNT(DISTINCT CASE WHEN d.DiemTongKet < 5 THEN sv.MaSV END) as SoSVKhongDat,
        AVG(d.DiemTongKet) as DiemTrungBinhLop
    FROM Lop l
    JOIN SinhVien sv ON l.MaLop = sv.MaLop
    LEFT JOIN Diem d ON sv.MaSV = d.MaSV
    WHERE l.MaLop = @MaLop
    AND d.HocKy = @HocKy
    AND d.NamHoc = @NamHoc
    GROUP BY l.MaLop, l.TenLop
END
GO

-- 9. Stored Procedure cập nhật trạng thái sinh viên
CREATE PROCEDURE sp_CapNhatTrangThaiSinhVien
    @MaSV VARCHAR(20),
    @TrangThaiMoi NVARCHAR(50)
AS
BEGIN
    BEGIN TRY
        IF @TrangThaiMoi NOT IN (N'Đang học', N'Bảo lưu', N'Nghỉ học', N'Tốt nghiệp', N'Đình chỉ')
        BEGIN
            RAISERROR(N'Trạng thái không hợp lệ!', 16, 1)
            RETURN
        END

        UPDATE SinhVien
        SET TrangThai = @TrangThaiMoi
        WHERE MaSV = @MaSV
    END TRY
    BEGIN CATCH
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE()
        RAISERROR(@ErrorMessage, 16, 1)
    END CATCH
END
GO

-- Tạo trigger để tự động cập nhật trạng thái dựa trên số tiền đã đóng
go
CREATE TRIGGER trg_CapNhatTrangThaiHocPhi
ON HocPhi
AFTER INSERT, UPDATE
AS
BEGIN
    UPDATE hp
    SET TrangThai = CASE
        WHEN i.SoTienDong = 0 THEN N'Chưa đóng'
        WHEN i.SoTienDong = i.SoTien THEN N'Đã đóng'
        WHEN i.SoTienDong < i.SoTien THEN N'Nợ'
    END,
    NgayDong = CASE
        WHEN i.SoTienDong > 0 AND i.NgayDong IS NULL THEN GETDATE()
        ELSE i.NgayDong
    END
    FROM HocPhi hp
    INNER JOIN inserted i ON hp.MaHocPhi = i.MaHocPhi
END
GO

---------------


-- Tạo bảng tạm để lưu dữ liệu
SELECT MaHocPhi, MaSV, HocKy, NamHoc, SoTien as SoTienPhaiDong, 
       CASE WHEN TrangThai = 'Đã đóng' THEN SoTien ELSE 0 END as SoTienDaDong,
       TrangThai, NgayDong
INTO #TempHocPhi
FROM HocPhi;

-- Xóa bảng cũ
DROP TABLE HocPhi;

-- Tạo lại bảng với cấu trúc mới
CREATE TABLE HocPhi (
    MaHocPhi INT IDENTITY(1,1) PRIMARY KEY,
    MaSV VARCHAR(20) NOT NULL,
    HocKy INT NOT NULL,
    NamHoc VARCHAR(10) NOT NULL,
    SoTienPhaiDong DECIMAL(18,2) NOT NULL,
    SoTienDaDong DECIMAL(18,2) NOT NULL DEFAULT 0,
    TrangThai NVARCHAR(50) NOT NULL,
    NgayDong DATE,
    CONSTRAINT FK_HocPhi_SinhVien FOREIGN KEY (MaSV) REFERENCES SinhVien(MaSV)
);

-- Chèn lại dữ liệu từ bảng tạm
SET IDENTITY_INSERT HocPhi ON;
INSERT INTO HocPhi (MaHocPhi, MaSV, HocKy, NamHoc, SoTienPhaiDong, SoTienDaDong, TrangThai, NgayDong)
SELECT MaHocPhi, MaSV, HocKy, NamHoc, SoTienPhaiDong, SoTienDaDong,
    CASE 
        WHEN SoTienDaDong >= SoTienPhaiDong THEN N'Đã đóng đủ'
        WHEN SoTienDaDong > 0 THEN N'Nợ [' + CAST(SoTienPhaiDong - SoTienDaDong AS VARCHAR) + N' đ]'
        ELSE N'Chưa đóng'
    END,
    NgayDong
FROM #TempHocPhi;
SET IDENTITY_INSERT HocPhi OFF;

-- Xóa bảng tạm
DROP TABLE #TempHocPhi;

-- Thêm dữ liệu mẫu cho bảng Khoa
INSERT INTO Khoa (MaKhoa, TenKhoa, NgayThanhLap, MoTa) VALUES
('CNTT', N'Công nghệ thông tin', '2010-01-01', N'Khoa đào tạo về CNTT')
GO

-- Thêm dữ liệu mẫu cho bảng NganhHoc
INSERT INTO NganhHoc (MaNganh, TenNganh, MaKhoa, MoTa) VALUES
('KTPM', N'Kỹ thuật phần mềm', 'CNTT', N'Chuyên ngành phát triển phần mềm'),
('HTTT', N'Hệ thống thông tin', 'CNTT', N'Chuyên ngành hệ thống thông tin')
GO

-- Thêm dữ liệu mẫu cho bảng Lớp
INSERT INTO Lop (MaLop, TenLop, MaNganh, NienKhoa, SiSo) VALUES
('KTPM01', N'Kỹ thuật phần mềm 1', 'KTPM', '2023-2027', 0),
('HTTT01', N'Hệ thống thông tin 1', 'HTTT', '2023-2027', 0)
GO

-- Thêm dữ liệu mẫu cho bảng MonHoc
INSERT INTO MonHoc (MaMH, TenMH, SoTinChi, MoTa) VALUES
('JAVA01', N'Lập trình Java', 3, N'Môn học về ngôn ngữ Java'),
('CSDL01', N'Cơ sở dữ liệu', 3, N'Môn học về database'),
('WEB01', N'Lập trình Web', 3, N'Môn học về phát triển web')
GO

-- Thêm dữ liệu mẫu cho bảng SinhVien
INSERT INTO SinhVien (MaSV, HoTen, NgaySinh, GioiTinh, DiaChi, SoDienThoai, Email, QueQuan, DanToc, TonGiao, MaLop, TrangThai) VALUES
('SV001', N'Nguyễn Văn An', '2005-05-15', N'Nam', N'Số 123 Đường Lê Thanh Nghị, Hai Bà Trưng, Hà Nội', '0901234567', 'an.nv@example.com', N'Số 123 Đường Lê Thanh Nghị, Hai Bà Trưng, Hà Nội', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV002', N'Trần Thị Bình', '2005-06-20', N'Nữ', N'Số 45 Đường Lạch Tray, Ngô Quyền, Hải Phòng', '0912345678', 'binh.tt@example.com', N'Số 45 Đường Lạch Tray, Ngô Quyền, Hải Phòng', N'Kinh', N'Phật giáo', 'KTPM01', N'Đang học'),
('SV003', N'Lê Văn Cường', '2005-07-25', N'Nam', N'Số 67 Đường Trần Hưng Đạo, TP Nam Định, Nam Định', '0923456789', 'cuong.lv@example.com', N'Số 67 Đường Trần Hưng Đạo, TP Nam Định, Nam Định', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV004', N'Phạm Thị Dung', '2005-08-30', N'Nữ', N'Số 89 Đường Lý Thường Kiệt, TP Thái Bình, Thái Bình', '0934567890', 'dung.pt@example.com', N'Số 89 Đường Lý Thường Kiệt, TP Thái Bình, Thái Bình', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV005', N'Hoàng Văn Em', '2005-09-05', N'Nam', N'Số 34 Đường Trần Phú, TP Phủ Lý, Hà Nam', '0945678901', 'em.hv@example.com', N'Số 34 Đường Trần Phú, TP Phủ Lý, Hà Nam', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV006', N'Ngô Thị Phương', '2005-10-10', N'Nữ', N'Số 56 Đường Giải Phóng, Hoàng Mai, Hà Nội', '0956789012', 'phuong.nt@example.com', N'Số 56 Đường Giải Phóng, Hoàng Mai, Hà Nội', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV007', N'Đỗ Văn Giang', '2005-11-15', N'Nam', N'Số 78 Đường Nguyễn Văn Cừ, TP Bắc Ninh, Bắc Ninh', '0967890123', 'giang.dv@example.com', N'Số 78 Đường Nguyễn Văn Cừ, TP Bắc Ninh, Bắc Ninh', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV008', N'Mai Thị Hoa', '2005-12-20', N'Nữ', N'Số 90 Đường Nguyễn Văn Linh, TP Hưng Yên, Hưng Yên', '0978901234', 'hoa.mt@example.com', N'Số 90 Đường Nguyễn Văn Linh, TP Hưng Yên, Hưng Yên', N'Kinh', N'Phật giáo', 'HTTT01', N'Đang học'),
('SV009', N'Vũ Văn Inh', '2005-01-25', N'Nam', N'Số 112 Đường Trường Chinh, Thanh Xuân, Hà Nội', '0989012345', 'inh.vv@example.com', N'Số 112 Đường Trường Chinh, Thanh Xuân, Hà Nội', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV010', N'Lý Thị Kim', '2005-02-28', N'Nữ', N'Số 23 Đường Ngô Quyền, TP Vĩnh Yên, Vĩnh Phúc', '0990123456', 'kim.lt@example.com', N'Số 23 Đường Ngô Quyền, TP Vĩnh Yên, Vĩnh Phúc', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV011', N'Trương Minh Long', '2005-03-15', N'Nam', N'Số 145 Đường Láng, Đống Đa, Hà Nội', '0901111222', 'long.tm@example.com', N'Số 145 Đường Láng, Đống Đa, Hà Nội', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV012', N'Nguyễn Thị Mai', '2005-04-20', N'Nữ', N'Số 67 Đường Lê Lợi, Ngô Quyền, Hải Phòng', '0912222333', 'mai.nt@example.com', N'Số 67 Đường Lê Lợi, Ngô Quyền, Hải Phòng', N'Kinh', N'Phật giáo', 'KTPM01', N'Đang học'),
('SV013', N'Phạm Văn Nam', '2005-05-25', N'Nam', N'Số 89 Đường Phan Chu Trinh, Hoàn Kiếm, Hà Nội', '0923333444', 'nam.pv@example.com', N'Số 89 Đường Phan Chu Trinh, Hoàn Kiếm, Hà Nội', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV014', N'Hoàng Thị Oanh', '2005-06-30', N'Nữ', N'Số 234 Đường Nguyễn Trãi, Thanh Xuân, Hà Nội', '0934444555', 'oanh.ht@example.com', N'Số 234 Đường Nguyễn Trãi, Thanh Xuân, Hà Nội', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV015', N'Lê Văn Phong', '2005-07-05', N'Nam', N'Số 56 Đường Trần Phú, TP Bắc Giang, Bắc Giang', '0945555666', 'phong.lv@example.com', N'Số 56 Đường Trần Phú, TP Bắc Giang, Bắc Giang', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV016', N'Trần Thị Quỳnh', '2005-08-10', N'Nữ', N'Số 78 Đường Bà Triệu, TP Hải Dương, Hải Dương', '0956666777', 'quynh.tt@example.com', N'Số 78 Đường Bà Triệu, TP Hải Dương, Hải Dương', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV017', N'Nguyễn Văn Rồng', '2005-09-15', N'Nam', N'Số 90 Đường Lý Thái Tổ, TP Bắc Ninh, Bắc Ninh', '0967777888', 'rong.nv@example.com', N'Số 90 Đường Lý Thái Tổ, TP Bắc Ninh, Bắc Ninh', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV018', N'Đặng Thị Sương', '2005-10-20', N'Nữ', N'Số 123 Đường Trần Duy Hưng, Cầu Giấy, Hà Nội', '0978888999', 'suong.dt@example.com', N'Số 123 Đường Trần Duy Hưng, Cầu Giấy, Hà Nội', N'Kinh', N'Phật giáo', 'HTTT01', N'Đang học'),
('SV019', N'Vũ Văn Thành', '2005-11-25', N'Nam', N'Số 45 Đường Quang Trung, TP Thái Nguyên, Thái Nguyên', '0989999000', 'thanh.vv@example.com', N'Số 45 Đường Quang Trung, TP Thái Nguyên, Thái Nguyên', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV020', N'Mai Thị Uyên', '2005-12-30', N'Nữ', N'Số 67 Đường Lê Duẩn, TP Việt Trì, Phú Thọ', '0990000111', 'uyen.mt@example.com', N'Số 67 Đường Lê Duẩn, TP Việt Trì, Phú Thọ', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV021', N'Nguyễn Thị Vân', '2005-01-05', N'Nữ', N'Số 89 Đường Hùng Vương, TP Yên Bái, Yên Bái', '0901222333', 'van.nt@example.com', N'Số 89 Đường Hùng Vương, TP Yên Bái, Yên Bái', N'Kinh', N'Phật giáo', 'KTPM01', N'Đang học'),
('SV022', N'Trần Văn Xuân', '2005-02-10', N'Nam', N'Số 123 Đường Trần Nhân Tông, TP Lạng Sơn, Lạng Sơn', '0912333444', 'xuan.tv@example.com', N'Số 123 Đường Trần Nhân Tông, TP Lạng Sơn, Lạng Sơn', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV023', N'Lê Thị Yến', '2005-03-15', N'Nữ', N'Số 45 Đường Lý Tự Trọng, TP Cao Bằng, Cao Bằng', '0923444555', 'yen.lt@example.com', N'Số 45 Đường Lý Tự Trọng, TP Cao Bằng, Cao Bằng', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV024', N'Phạm Văn Zũng', '2005-04-20', N'Nam', N'Số 67 Đường Nguyễn Du, TP Điện Biên Phủ, Điện Biên', '0934555666', 'zung.pv@example.com', N'Số 67 Đường Nguyễn Du, TP Điện Biên Phủ, Điện Biên', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV025', N'Hoàng Thị Ánh', '2005-05-25', N'Nữ', N'Số 234 Đường Trần Phú, TP Sơn La, Sơn La', '0945666777', 'anh.ht@example.com', N'Số 234 Đường Trần Phú, TP Sơn La, Sơn La', N'Thái', N'Không', 'KTPM01', N'Đang học'),
('SV026', N'Đỗ Văn Bách', '2005-06-30', N'Nam', N'Số 56 Đường Lê Lợi, TP Lai Châu, Lai Châu', '0956777888', 'bach.dv@example.com', N'Số 56 Đường Lê Lợi, TP Lai Châu, Lai Châu', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV027', N'Ngô Thị Châu', '2005-07-05', N'Nữ', N'Số 78 Đường Nguyễn Huệ, TP Lào Cai, Lào Cai', '0967888999', 'chau.nt@example.com', N'Số 78 Đường Nguyễn Huệ, TP Lào Cai, Lào Cai', N'Kinh', N'Phật giáo', 'KTPM01', N'Đang học'),
('SV028', N'Vũ Văn Dũng', '2005-08-10', N'Nam', N'Số 90 Đường Quang Trung, TP Hòa Bình, Hòa Bình', '0978999000', 'dung.vv@example.com', N'Số 90 Đường Quang Trung, TP Hòa Bình, Hòa Bình', N'Mường', N'Không', 'HTTT01', N'Đang học'),
('SV029', N'Mai Thị Giang', '2005-09-15', N'Nữ', N'Số 112 Đường Phan Đình Phùng, Tây Hồ, Hà Nội', '0989000111', 'giang.mt@example.com', N'Số 112 Đường Phan Đình Phùng, Tây Hồ, Hà Nội', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV030', N'Lý Văn Hải', '2005-10-20', N'Nam', N'Số 145 Đường Bạch Đằng, Hồng Bàng, Hải Phòng', '0990111222', 'hai.lv@example.com', N'Số 145 Đường Bạch Đằng, Hồng Bàng, Hải Phòng', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV031', N'Trương Thị Hương', '2005-11-25', N'Nữ', N'Số 67 Đường Trần Hưng Đạo, TP Thái Bình, Thái Bình', '0901333444', 'huong.tt@example.com', N'Số 67 Đường Trần Hưng Đạo, TP Thái Bình, Thái Bình', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV032', N'Nguyễn Văn Khánh', '2005-12-30', N'Nam', N'Số 89 Đường Lê Thánh Tông, TP Nam Định, Nam Định', '0912444555', 'khanh.nv@example.com', N'Số 89 Đường Lê Thánh Tông, TP Nam Định, Nam Định', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV033', N'Trần Thị Linh', '2005-01-05', N'Nữ', N'Số 234 Đường Trần Phú, TP Ninh Bình, Ninh Bình', '0923555666', 'linh.tt@example.com', N'Số 234 Đường Trần Phú, TP Ninh Bình, Ninh Bình', N'Kinh', N'Phật giáo', 'KTPM01', N'Đang học'),
('SV034', N'Phạm Văn Minh', '2005-02-10', N'Nam', N'Số 56 Đường Lý Thường Kiệt, TP Thanh Hóa, Thanh Hóa', '0934666777', 'minh.pv@example.com', N'Số 56 Đường Lý Thường Kiệt, TP Thanh Hóa, Thanh Hóa', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV035', N'Hoàng Thị Ngọc', '2005-03-15', N'Nữ', N'Số 78 Đường Lê Lai, TP Vinh, Nghệ An', '0945777888', 'ngoc.ht@example.com', N'Số 78 Đường Lê Lai, TP Vinh, Nghệ An', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV036', N'Đỗ Văn Phúc', '2005-04-20', N'Nam', N'Số 90 Đường Nguyễn Trãi, TP Hà Tĩnh, Hà Tĩnh', '0956888999', 'phuc.dv@example.com', N'Số 90 Đường Nguyễn Trãi, TP Hà Tĩnh, Hà Tĩnh', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV037', N'Ngô Thị Quỳnh', '2005-05-25', N'Nữ', N'Số 112 Đường Trần Hưng Đạo, TP Đồng Hới, Quảng Bình', '0967999000', 'quynh.nt2@example.com', N'Số 112 Đường Trần Hưng Đạo, TP Đồng Hới, Quảng Bình', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV038', N'Vũ Văn Sơn', '2005-06-30', N'Nam', N'Số 145 Đường Lê Duẩn, TP Huế, Thừa Thiên Huế', '0978000111', 'son.vv@example.com', N'Số 145 Đường Lê Duẩn, TP Huế, Thừa Thiên Huế', N'Kinh', N'Phật giáo', 'HTTT01', N'Đang học'),
('SV039', N'Mai Thị Thảo', '2005-07-05', N'Nữ', N'Số 67 Đường Phan Chu Trinh, TP Đà Nẵng, Đà Nẵng', '0989111222', 'thao.mt@example.com', N'Số 67 Đường Phan Chu Trinh, TP Đà Nẵng, Đà Nẵng', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV040', N'Lý Văn Uy', '2005-08-10', N'Nam', N'Số 89 Đường Nguyễn Văn Linh, TP Tam Kỳ, Quảng Nam', '0990222333', 'uy.lv@example.com', N'Số 89 Đường Nguyễn Văn Linh, TP Tam Kỳ, Quảng Nam', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV041', N'Nguyễn Thị Việt', '2005-09-15', N'Nữ', N'Số 123 Đường Hùng Vương, TP Quy Nhơn, Bình Định', '0901444555', 'viet.nt@example.com', N'Số 123 Đường Hùng Vương, TP Quy Nhơn, Bình Định', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV042', N'Trần Văn Xuyên', '2005-10-20', N'Nam', N'Số 45 Đường Quang Trung, TP Tuy Hòa, Phú Yên', '0912555666', 'xuyen.tv@example.com', N'Số 45 Đường Quang Trung, TP Tuy Hòa, Phú Yên', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV043', N'Lê Thị Yến Nhi', '2005-11-25', N'Nữ', N'Số 67 Đường Nguyễn Huệ, TP Nha Trang, Khánh Hòa', '0923666777', 'nhi.lty@example.com', N'Số 67 Đường Nguyễn Huệ, TP Nha Trang, Khánh Hòa', N'Kinh', N'Phật giáo', 'KTPM01', N'Đang học'),
('SV044', N'Phạm Văn Anh', '2005-12-30', N'Nam', N'Số 89 Đường Trần Phú, TP Phan Rang, Ninh Thuận', '0934777888', 'anh.pv@example.com', N'Số 89 Đường Trần Phú, TP Phan Rang, Ninh Thuận', N'Chăm', N'Không', 'HTTT01', N'Đang học'),
('SV045', N'Hoàng Thị Bích', '2005-01-05', N'Nữ', N'Số 234 Đường Lê Duẩn, TP Phan Thiết, Bình Thuận', '0945888999', 'bich.ht@example.com', N'Số 234 Đường Lê Duẩn, TP Phan Thiết, Bình Thuận', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV046', N'Đỗ Văn Cường', '2005-02-10', N'Nam', N'Số 56 Đường Nguyễn Tất Thành, TP Kon Tum, Kon Tum', '0956999000', 'cuong.dv2@example.com', N'Số 56 Đường Nguyễn Tất Thành, TP Kon Tum, Kon Tum', N'Kinh', N'Không', 'HTTT01', N'Đang học'),
('SV047', N'Ngô Thị Diệu', '2005-03-15', N'Nữ', N'Số 78 Đường Lê Lợi, TP Pleiku, Gia Lai', '0967000111', 'dieu.nt@example.com', N'Số 78 Đường Lê Lợi, TP Pleiku, Gia Lai', N'Kinh', N'Không', 'KTPM01', N'Đang học'),
('SV048', N'Vũ Văn Đức', '2005-04-20', N'Nam', N'Số 90 Đường Nguyễn Văn Cừ, TP Buôn Ma Thuột, Đắk Lắk', '0978111222', 'duc.vv@example.com', N'Số 90 Đường Nguyễn Văn Cừ, TP Buôn Ma Thuột, Đắk Lắk', N'Ê Đê', N'Không', 'HTTT01', N'Đang học'),
('SV049', N'Mai Thị Hạnh', '2005-05-25', N'Nữ', N'Số 112 Đường Hùng Vương, TP Gia Nghĩa, Đắk Nông', '0989222333', 'hanh.mt@example.com', N'Số 112 Đường Hùng Vương, TP Gia Nghĩa, Đắk Nông', N'Kinh', N'Phật giáo', 'KTPM01', N'Đang học'),
('SV050', N'Lý Văn Khôi', '2005-06-30', N'Nam', N'Số 145 Đường Trần Hưng Đạo, TP Đà Lạt, Lâm Đồng', '0990333444', 'khoi.lv@example.com', N'Số 145 Đường Trần Hưng Đạo, TP Đà Lạt, Lâm Đồng', N'Kinh', N'Không', 'HTTT01', N'Đang học')
GO

-- Thêm dữ liệu mẫu cho bảng Diem
INSERT INTO Diem (MaSV, MaMH, HocKy, NamHoc, DiemQuaTrinh, DiemThi, DiemTongKet) VALUES
-- Năm học 2021-2022
('SV001', 'JAVA01', 1, '2021-2022', 8.5, 8.0, 8.2),
('SV001', 'CSDL01', 1, '2021-2022', 7.5, 8.0, 7.8),
('SV001', 'WEB01', 2, '2021-2022', 8.0, 8.5, 8.3),
('SV002', 'JAVA01', 1, '2021-2022', 7.0, 7.5, 7.3),
('SV002', 'CSDL01', 2, '2021-2022', 8.5, 8.0, 8.2),
('SV003', 'WEB01', 1, '2021-2022', 9.0, 8.5, 8.7),
('SV003', 'JAVA01', 2, '2021-2022', 8.0, 8.5, 8.3),

-- Năm học 2022-2023
('SV004', 'CSDL01', 1, '2022-2023', 7.5, 8.0, 7.8),
('SV004', 'WEB01', 2, '2022-2023', 8.5, 8.0, 8.2),
('SV005', 'JAVA01', 1, '2022-2023', 9.0, 9.0, 9.0),
('SV005', 'CSDL01', 2, '2022-2023', 8.5, 9.0, 8.8),
('SV006', 'WEB01', 1, '2022-2023', 7.0, 7.5, 7.3),
('SV006', 'JAVA01', 2, '2022-2023', 8.0, 7.5, 7.7),
('SV007', 'CSDL01', 1, '2022-2023', 8.5, 8.0, 8.2),
('SV007', 'WEB01', 2, '2022-2023', 7.5, 8.0, 7.8),

-- Năm học 2023-2024
('SV008', 'JAVA01', 1, '2023-2024', 8.0, 8.5, 8.3),
('SV008', 'CSDL01', 2, '2023-2024', 7.0, 7.5, 7.3),
('SV009', 'WEB01', 1, '2023-2024', 9.0, 8.5, 8.7),
('SV009', 'JAVA01', 2, '2023-2024', 8.5, 8.0, 8.2),
('SV010', 'CSDL01', 1, '2023-2024', 7.5, 8.0, 7.8),
('SV010', 'WEB01', 2, '2023-2024', 8.0, 8.5, 8.3),

-- Thêm điểm cho các sinh viên mới (SV011-SV020) - Năm học 2023-2024
('SV011', 'JAVA01', 1, '2023-2024', 8.5, 8.0, 8.2),
('SV011', 'CSDL01', 2, '2023-2024', 7.0, 7.5, 7.3),
('SV012', 'WEB01', 1, '2023-2024', 9.0, 8.5, 8.7),
('SV012', 'JAVA01', 2, '2023-2024', 8.0, 8.5, 8.3),
('SV013', 'CSDL01', 1, '2023-2024', 7.5, 8.0, 7.8),
('SV013', 'WEB01', 2, '2023-2024', 8.5, 8.0, 8.2),
('SV014', 'JAVA01', 1, '2023-2024', 9.0, 9.0, 9.0),
('SV014', 'CSDL01', 2, '2023-2024', 8.5, 9.0, 8.8),
('SV015', 'WEB01', 1, '2023-2024', 7.0, 7.5, 7.3),

-- Thêm điểm cho các sinh viên (SV016-SV025) - Năm học 2024-2025
('SV016', 'JAVA01', 1, '2024-2025', 8.0, 8.5, 8.3),
('SV016', 'CSDL01', 2, '2024-2025', 7.5, 8.0, 7.8),
('SV017', 'WEB01', 1, '2024-2025', 9.0, 8.5, 8.7),
('SV017', 'JAVA01', 2, '2024-2025', 8.5, 8.0, 8.2),
('SV018', 'CSDL01', 1, '2024-2025', 7.0, 7.5, 7.3),
('SV018', 'WEB01', 2, '2024-2025', 8.0, 8.5, 8.3),
('SV019', 'JAVA01', 1, '2024-2025', 8.5, 8.0, 8.2),
('SV019', 'CSDL01', 2, '2024-2025', 7.5, 8.0, 7.8),
('SV020', 'WEB01', 1, '2024-2025', 9.0, 8.5, 8.7),
('SV020', 'JAVA01', 2, '2024-2025', 8.0, 8.5, 8.3),

-- Thêm điểm cho các sinh viên (SV021-SV030) - Năm học đa dạng
('SV021', 'CSDL01', 1, '2021-2022', 7.5, 8.0, 7.8),
('SV021', 'WEB01', 2, '2021-2022', 8.5, 8.0, 8.2),
('SV022', 'JAVA01', 1, '2022-2023', 9.0, 9.0, 9.0),
('SV022', 'CSDL01', 2, '2022-2023', 8.5, 9.0, 8.8),
('SV023', 'WEB01', 1, '2023-2024', 7.0, 7.5, 7.3),
('SV023', 'JAVA01', 2, '2023-2024', 8.0, 7.5, 7.7),
('SV024', 'CSDL01', 1, '2024-2025', 8.5, 8.0, 8.2),
('SV024', 'WEB01', 2, '2024-2025', 7.5, 8.0, 7.8),
('SV025', 'JAVA01', 1, '2021-2022', 8.0, 8.5, 8.3),
('SV025', 'CSDL01', 2, '2021-2022', 7.0, 7.5, 7.3),

-- Thêm điểm cho các sinh viên (SV031-SV040) - Năm học đa dạng
('SV031', 'WEB01', 1, '2022-2023', 9.0, 8.5, 8.7),
('SV031', 'JAVA01', 2, '2022-2023', 8.5, 8.0, 8.2),
('SV032', 'CSDL01', 1, '2023-2024', 7.5, 8.0, 7.8),
('SV032', 'WEB01', 2, '2023-2024', 8.0, 8.5, 8.3),
('SV033', 'JAVA01', 1, '2024-2025', 8.5, 8.0, 8.2),
('SV033', 'CSDL01', 2, '2024-2025', 7.0, 7.5, 7.3),
('SV034', 'WEB01', 1, '2021-2022', 9.0, 8.5, 8.7),
('SV034', 'JAVA01', 2, '2021-2022', 8.0, 8.5, 8.3),
('SV035', 'CSDL01', 1, '2022-2023', 7.5, 8.0, 7.8),
('SV035', 'WEB01', 2, '2022-2023', 8.5, 8.0, 8.2),

-- Thêm điểm cho các sinh viên (SV041-SV050) - Năm học đa dạng
('SV041', 'JAVA01', 1, '2023-2024', 9.0, 9.0, 9.0),
('SV041', 'CSDL01', 2, '2023-2024', 8.5, 9.0, 8.8),
('SV042', 'WEB01', 1, '2024-2025', 7.0, 7.5, 7.3),
('SV042', 'JAVA01', 2, '2024-2025', 8.0, 7.5, 7.7),
('SV043', 'CSDL01', 1, '2021-2022', 8.5, 8.0, 8.2),
('SV043', 'WEB01', 2, '2021-2022', 7.5, 8.0, 7.8),
('SV044', 'JAVA01', 1, '2022-2023', 8.0, 8.5, 8.3),
('SV044', 'CSDL01', 2, '2022-2023', 7.0, 7.5, 7.3),
('SV045', 'WEB01', 1, '2023-2024', 9.0, 8.5, 8.7),
('SV045', 'JAVA01', 2, '2023-2024', 8.5, 8.0, 8.2),
('SV046', 'CSDL01', 1, '2024-2025', 7.5, 8.0, 7.8),
('SV046', 'WEB01', 2, '2024-2025', 8.0, 8.5, 8.3),
('SV047', 'JAVA01', 1, '2021-2022', 8.5, 8.0, 8.2),
('SV047', 'CSDL01', 2, '2021-2022', 7.0, 7.5, 7.3),
('SV048', 'WEB01', 1, '2022-2023', 9.0, 8.5, 8.7),
('SV048', 'JAVA01', 2, '2022-2023', 8.0, 8.5, 8.3),
('SV049', 'CSDL01', 1, '2023-2024', 7.5, 8.0, 7.8),
('SV049', 'WEB01', 2, '2023-2024', 8.5, 8.0, 8.2),
('SV050', 'JAVA01', 1, '2024-2025', 9.0, 9.0, 9.0),
('SV050', 'CSDL01', 2, '2024-2025', 8.5, 9.0, 8.8)
GO

-- Thêm dữ liệu mẫu cho bảng HocPhi
INSERT INTO HocPhi (MaSV, HocKy, NamHoc, SoTienPhaiDong, SoTienDaDong, TrangThai, NgayDong) VALUES
-- Năm học 2021-2022
('SV001', 1, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2021-09-01'),
('SV001', 2, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2022-01-15'),
('SV002', 1, '2021-2022', 10000000, 8000000, N'Nợ [2000000 đ]', '2021-09-10'),
('SV002', 2, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2022-01-20'),
('SV003', 1, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2021-09-05'),
('SV003', 2, '2021-2022', 10000000, 5000000, N'Nợ [5000000 đ]', '2022-02-01'),

-- Năm học 2022-2023
('SV004', 1, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2022-09-01'),
('SV004', 2, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2023-01-15'),
('SV005', 1, '2022-2023', 11000000, 6000000, N'Nợ [5000000 đ]', '2022-09-20'),
('SV005', 2, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2023-01-25'),
('SV006', 1, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2022-09-10'),
('SV006', 2, '2022-2023', 11000000, 0, N'Chưa đóng', NULL),

-- Năm học 2023-2024
('SV007', 1, '2023-2024', 12000000, 12000000, N'Đã đóng đủ', '2023-09-01'),
('SV007', 2, '2023-2024', 12000000, 6000000, N'Nợ [6000000 đ]', '2024-01-20'),
('SV008', 1, '2023-2024', 12000000, 0, N'Chưa đóng', NULL),
('SV008', 2, '2023-2024', 12000000, 12000000, N'Đã đóng đủ', '2024-01-15'),
('SV009', 1, '2023-2024', 12000000, 7000000, N'Nợ [5000000 đ]', '2023-09-25'),
('SV009', 2, '2023-2024', 12000000, 12000000, N'Đã đóng đủ', '2024-01-30'),

-- Năm học 2024-2025 (Dự kiến)
('SV010', 1, '2024-2025', 13000000, 13000000, N'Đã đóng đủ', '2024-09-01'),
('SV011', 1, '2024-2025', 13000000, 7000000, N'Nợ [6000000 đ]', '2024-09-10'),
('SV012', 1, '2024-2025', 13000000, 0, N'Chưa đóng', NULL),

-- Thêm học phí cho sinh viên 13-20 (2021-2022)
('SV013', 1, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2021-09-15'),
('SV013', 2, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2022-01-20'),
('SV014', 1, '2021-2022', 10000000, 6000000, N'Nợ [4000000 đ]', '2021-09-20'),
('SV014', 2, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2022-02-01'),
('SV015', 1, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2021-09-05'),
('SV015', 2, '2021-2022', 10000000, 5000000, N'Nợ [5000000 đ]', '2022-01-25'),

-- Thêm học phí cho sinh viên 16-25 (2022-2023)
('SV016', 1, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2022-09-05'),
('SV016', 2, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2023-01-20'),
('SV017', 1, '2022-2023', 11000000, 7000000, N'Nợ [4000000 đ]', '2022-09-15'),
('SV017', 2, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2023-02-01'),
('SV018', 1, '2022-2023', 11000000, 0, N'Chưa đóng', NULL),
('SV018', 2, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2023-01-25'),

-- Thêm học phí cho sinh viên 26-35 (2023-2024)
('SV026', 1, '2023-2024', 12000000, 12000000, N'Đã đóng đủ', '2023-09-10'),
('SV026', 2, '2023-2024', 12000000, 12000000, N'Đã đóng đủ', '2024-01-15'),
('SV027', 1, '2023-2024', 12000000, 8000000, N'Nợ [4000000 đ]', '2023-09-20'),
('SV027', 2, '2023-2024', 12000000, 12000000, N'Đã đóng đủ', '2024-02-01'),
('SV028', 1, '2023-2024', 12000000, 0, N'Chưa đóng', NULL),
('SV028', 2, '2023-2024', 12000000, 12000000, N'Đã đóng đủ', '2024-01-25'),

-- Thêm học phí cho sinh viên 36-50 (2024-2025)
('SV036', 1, '2024-2025', 13000000, 13000000, N'Đã đóng đủ', '2024-09-05'),
('SV037', 1, '2024-2025', 13000000, 8000000, N'Nợ [5000000 đ]', '2024-09-15'),
('SV038', 1, '2024-2025', 13000000, 13000000, N'Đã đóng đủ', '2024-09-01'),
('SV039', 1, '2024-2025', 13000000, 0, N'Chưa đóng', NULL),
('SV040', 1, '2024-2025', 13000000, 7000000, N'Nợ [6000000 đ]', '2024-09-20'),

-- Thêm học phí cho các sinh viên còn lại với phân bố đa dạng
('SV041', 1, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2021-09-10'),
('SV041', 2, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2022-01-15'),
('SV042', 1, '2022-2023', 11000000, 6000000, N'Nợ [5000000 đ]', '2022-09-20'),
('SV042', 2, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2023-01-25'),
('SV043', 1, '2023-2024', 12000000, 12000000, N'Đã đóng đủ', '2023-09-05'),
('SV043', 2, '2023-2024', 12000000, 0, N'Chưa đóng', NULL),
('SV044', 1, '2024-2025', 13000000, 7000000, N'Nợ [6000000 đ]', '2024-09-15'),
('SV045', 1, '2021-2022', 10000000, 10000000, N'Đã đóng đủ', '2021-09-01'),
('SV045', 2, '2021-2022', 10000000, 5000000, N'Nợ [5000000 đ]', '2022-01-20'),
('SV046', 1, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2022-09-10'),
('SV046', 2, '2022-2023', 11000000, 11000000, N'Đã đóng đủ', '2023-01-15'),
('SV047', 1, '2023-2024', 12000000, 0, N'Chưa đóng', NULL),
('SV047', 2, '2023-2024', 12000000, 12000000, N'Đã đóng đủ', '2024-01-25'),
('SV048', 1, '2024-2025', 13000000, 13000000, N'Đã đóng đủ', '2024-09-05'),
('SV049', 1, '2024-2025', 13000000, 8000000, N'Nợ [5000000 đ]', '2024-09-20'),
('SV050', 1, '2024-2025', 13000000, 13000000, N'Đã đóng đủ', '2024-09-01')
GO

-- Stored Procedure thống kê học phí
GO
CREATE PROCEDURE sp_ThongKeHocPhi
    @MaLop VARCHAR(10) = NULL,
    @MaKhoa VARCHAR(10) = NULL,
    @NamHoc VARCHAR(20) = NULL,
    @HocKy INT = NULL
AS
BEGIN
    -- Tạo bảng tạm để lưu kết quả thống kê
    CREATE TABLE #TempThongKe (
        DonVi NVARCHAR(100),
        LoaiDonVi NVARCHAR(50),
        TongSoSV INT,
        TongSoTienPhaiDong DECIMAL(18,2),
        TongSoTienDaDong DECIMAL(18,2),
        TongSoTienConNo DECIMAL(18,2),
        SoSVDaDongDu INT,
        SoSVConNo INT,
        SoSVChuaDong INT
    )

    -- Thống kê theo lớp
    IF @MaLop IS NOT NULL
    BEGIN
        INSERT INTO #TempThongKe
        SELECT 
            l.TenLop as DonVi,
            N'Lớp' as LoaiDonVi,
            COUNT(DISTINCT sv.MaSV) as TongSoSV,
            SUM(hp.SoTienPhaiDong) as TongSoTienPhaiDong,
            SUM(hp.SoTienDaDong) as TongSoTienDaDong,
            SUM(hp.SoTienPhaiDong - hp.SoTienDaDong) as TongSoTienConNo,
            SUM(CASE WHEN hp.TrangThai = N'Đã đóng đủ' THEN 1 ELSE 0 END) as SoSVDaDongDu,
            SUM(CASE WHEN hp.TrangThai LIKE N'Nợ%' THEN 1 ELSE 0 END) as SoSVConNo,
            SUM(CASE WHEN hp.TrangThai = N'Chưa đóng' THEN 1 ELSE 0 END) as SoSVChuaDong
        FROM Lop l
        JOIN SinhVien sv ON l.MaLop = sv.MaLop
        LEFT JOIN HocPhi hp ON sv.MaSV = hp.MaSV
        WHERE l.MaLop = @MaLop
        AND (@NamHoc IS NULL OR hp.NamHoc = @NamHoc)
        AND (@HocKy IS NULL OR hp.HocKy = @HocKy)
        GROUP BY l.TenLop
    END
    -- Thống kê theo khoa
    ELSE IF @MaKhoa IS NOT NULL
    BEGIN
        INSERT INTO #TempThongKe
        SELECT 
            k.TenKhoa as DonVi,
            N'Khoa' as LoaiDonVi,
            COUNT(DISTINCT sv.MaSV) as TongSoSV,
            SUM(hp.SoTienPhaiDong) as TongSoTienPhaiDong,
            SUM(hp.SoTienDaDong) as TongSoTienDaDong,
            SUM(hp.SoTienPhaiDong - hp.SoTienDaDong) as TongSoTienConNo,
            SUM(CASE WHEN hp.TrangThai = N'Đã đóng đủ' THEN 1 ELSE 0 END) as SoSVDaDongDu,
            SUM(CASE WHEN hp.TrangThai LIKE N'Nợ%' THEN 1 ELSE 0 END) as SoSVConNo,
            SUM(CASE WHEN hp.TrangThai = N'Chưa đóng' THEN 1 ELSE 0 END) as SoSVChuaDong
        FROM Khoa k
        JOIN NganhHoc nh ON k.MaKhoa = nh.MaKhoa
        JOIN Lop l ON nh.MaNganh = l.MaNganh
        JOIN SinhVien sv ON l.MaLop = sv.MaLop
        LEFT JOIN HocPhi hp ON sv.MaSV = hp.MaSV
        WHERE k.MaKhoa = @MaKhoa
        AND (@NamHoc IS NULL OR hp.NamHoc = @NamHoc)
        AND (@HocKy IS NULL OR hp.HocKy = @HocKy)
        GROUP BY k.TenKhoa
    END
    -- Thống kê tổng thể
    ELSE
    BEGIN
        INSERT INTO #TempThongKe
        SELECT 
            N'Toàn trường' as DonVi,
            N'Tổng thể' as LoaiDonVi,
            COUNT(DISTINCT sv.MaSV) as TongSoSV,
            SUM(hp.SoTienPhaiDong) as TongSoTienPhaiDong,
            SUM(hp.SoTienDaDong) as TongSoTienDaDong,
            SUM(hp.SoTienPhaiDong - hp.SoTienDaDong) as TongSoTienConNo,
            SUM(CASE WHEN hp.TrangThai = N'Đã đóng đủ' THEN 1 ELSE 0 END) as SoSVDaDongDu,
            SUM(CASE WHEN hp.TrangThai LIKE N'Nợ%' THEN 1 ELSE 0 END) as SoSVConNo,
            SUM(CASE WHEN hp.TrangThai = N'Chưa đóng' THEN 1 ELSE 0 END) as SoSVChuaDong
        FROM SinhVien sv
        LEFT JOIN HocPhi hp ON sv.MaSV = hp.MaSV
        WHERE (@NamHoc IS NULL OR hp.NamHoc = @NamHoc)
        AND (@HocKy IS NULL OR hp.HocKy = @HocKy)
    END

    -- Thêm thông tin tỷ lệ phần trăm
    SELECT 
        DonVi,
        LoaiDonVi,
        TongSoSV,
        TongSoTienPhaiDong,
        TongSoTienDaDong,
        TongSoTienConNo,
        SoSVDaDongDu,
        CAST(CAST(SoSVDaDongDu AS FLOAT) * 100 / NULLIF(TongSoSV, 0) AS DECIMAL(5,2)) as TyLeDaDongDu,
        SoSVConNo,
        CAST(CAST(SoSVConNo AS FLOAT) * 100 / NULLIF(TongSoSV, 0) AS DECIMAL(5,2)) as TyLeConNo,
        SoSVChuaDong,
        CAST(CAST(SoSVChuaDong AS FLOAT) * 100 / NULLIF(TongSoSV, 0) AS DECIMAL(5,2)) as TyLeChuaDong
    FROM #TempThongKe

    -- Xóa bảng tạm
    DROP TABLE #TempThongKe
END
GO

-- Ví dụ sử dụng:
-- Thống kê toàn trường
-- EXEC sp_ThongKeHocPhi

-- Thống kê theo khoa
-- EXEC sp_ThongKeHocPhi @MaKhoa = 'CNTT'

-- Thống kê theo lớp và năm học
-- EXEC sp_ThongKeHocPhi @MaLop = 'KTPM01', @NamHoc = '2023-2024'

-- Thống kê theo lớp, năm học và học kỳ
-- EXEC sp_ThongKeHocPhi @MaLop = 'KTPM01', @NamHoc = '2023-2024', @HocKy = 1
