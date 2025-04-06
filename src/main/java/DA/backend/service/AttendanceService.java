package DA.backend.service;

import DA.backend.dto.*;
import DA.backend.entity.*;
import DA.backend.enums.AttendanceCodeType;
import DA.backend.enums.AttendanceStatus;
import DA.backend.repository.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.*;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceCodeService attendanceCodeService;

    @Autowired
    private AttendanceAuditLogRepository attendanceAuditLogRepository;

    @Autowired
    private AttendanceModificationRequestRepository modificationRequestRepository;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private WorkingTimeConfigRepository configRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    private static final LocalTime WORK_START_TIME = LocalTime.of(8, 0);
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(8, 15);

    // Tạo mã check-in/out
    public String generateAttendanceCode(String userId, AttendanceCodeType type) {
        return attendanceCodeService.generateQRContent(userId, type);
    }

    public Attendance checkIn(String userId, String code, Double latitude, Double longitude, String address) {
        // Lấy và kiểm tra config
        WorkingTimeConfig config = configRepository.findActive();
        if (config == null) {
            config = createDefaultConfig();
        }

        // Kiểm tra ngày làm việc
        if (!isWorkingDay(LocalDate.now())) {
            throw new RuntimeException("Cannot check-in on non-working day");
        }

        // Kiểm tra xem user có đang nghỉ phép không
        if (leaveService.isUserOnLeave(userId, LocalDateTime.now())) {
            throw new RuntimeException("User is currently on leave");
        }

        // Xác thực mã
        if (!attendanceCodeService.validateCode(userId, code, AttendanceCodeType.CHECK_IN)) {
            throw new RuntimeException("Invalid or expired code");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra đã check-in chưa
        if (attendanceRepository.findLatestAttendanceByUserId(userId)
                .filter(a -> a.getCheckOutTime() == null)
                .isPresent()) {
            throw new RuntimeException("Already checked in");
        }

        LocalTime currentTime = LocalTime.now();
        LocalTime workStartTime = config.getWorkStartTime() != null ?
                config.getWorkStartTime() : LocalTime.of(8, 0);

        // Kiểm tra thời gian check-in
        LocalTime earliestCheckIn = workStartTime.minusMinutes(config.getEarlyCheckInMinutes());
        LocalTime latestCheckIn = workStartTime.plusMinutes(config.getLateCheckInMinutes());

        if (currentTime.isBefore(earliestCheckIn)) {
            throw new RuntimeException("Too early to check in. Earliest allowed time is: " + earliestCheckIn);
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setCheckInLatitude(latitude);
        attendance.setCheckInLongitude(longitude);
        attendance.setCheckInAddress(address);
        attendance.setCheckInCode(code);

        // Xác định status chỉ sử dụng các enum hiện có
        if (currentTime.isAfter(latestCheckIn)) {
            attendance.setStatus(AttendanceStatus.LATE.name());
        } else {
            attendance.setStatus(AttendanceStatus.PRESENT.name());
        }

        return attendanceRepository.save(attendance);
    }

    public Attendance checkOut(String userId, String code, Double latitude, Double longitude, String address) {
        WorkingTimeConfig config = configRepository.findActive();
        if (config == null) {
          config = createDefaultConfig();
        }

        if (!attendanceCodeService.validateCode(userId, code, AttendanceCodeType.CHECK_OUT)) {
            throw new RuntimeException("Invalid or expired code");
        }

        Attendance attendance = attendanceRepository.findLatestAttendanceByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No check-in record found"));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already checked out");
        }

        LocalDateTime checkOutTime = LocalDateTime.now();
        LocalTime workEndTime = config.getWorkEndTime() != null ?
                config.getWorkEndTime() : LocalTime.of(17, 30);

        attendance.setCheckOutTime(checkOutTime);
        attendance.setCheckOutLatitude(latitude);
        attendance.setCheckOutLongitude(longitude);
        attendance.setCheckOutAddress(address);
        attendance.setCheckOutCode(code);

        Duration workDuration = Duration.between(attendance.getCheckInTime(), checkOutTime);
        double workedHours = workDuration.toMinutes() / 60.0;
        attendance.setWorkingHours(workedHours);

        double minWorkingHours = config.getMinWorkingHours() != null ? config.getMinWorkingHours() : 8.0;
        double maxWorkingHours = config.getMaxWorkingHours() != null ? config.getMaxWorkingHours() : 12.0;

        // Cập nhật trạng thái theo enum
        if (workedHours < minWorkingHours) {
            attendance.setStatus(AttendanceStatus.ABSENT.name());
        } else if (checkOutTime.toLocalTime().isBefore(workEndTime)) {
            attendance.setStatus(AttendanceStatus.ABSENT.name());
        } else if (attendance.getStatus().equals(AttendanceStatus.LATE.name())) {
            attendance.setStatus(AttendanceStatus.LATE.name()); // Giữ nguyên LATE
        } else {
            attendance.setStatus(AttendanceStatus.PRESENT.name());
        }

        return attendanceRepository.save(attendance);
    }

    private boolean isWorkingDay(LocalDate date) {
        // Kiểm tra ngày nghỉ lễ
        if (holidayRepository.existsByDate(date)) {
            return false;
        }

        // Lấy và kiểm tra config
        WorkingTimeConfig config = configRepository.findActive();
        if (config == null) {
            // Nếu không có config, mặc định làm việc từ thứ 2 đến thứ 6
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            return dayOfWeek != DayOfWeek.SUNDAY;
//            return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return switch (dayOfWeek) {
            case MONDAY -> config.isMonday();
            case TUESDAY -> config.isTuesday();
            case WEDNESDAY -> config.isWednesday();
            case THURSDAY -> config.isThursday();
            case FRIDAY -> config.isFriday();
            case SATURDAY -> config.isSaturday();
            case SUNDAY -> config.isSunday();
        };
    }
    private WorkingTimeConfig createDefaultConfig() {
        WorkingTimeConfig defaultConfig = new WorkingTimeConfig();
        defaultConfig.setWorkStartTime(LocalTime.of(8, 0));  // 8:00 AM
        defaultConfig.setWorkEndTime(LocalTime.of(17, 30));  // 5:30 PM
        defaultConfig.setEarlyCheckInMinutes(60);  // Cho phép check in sớm 1 tiếng
        defaultConfig.setLateCheckInMinutes(15);   // Cho phép check in muộn 15 phút
        defaultConfig.setMinWorkingHours(8.0);     // 8 tiếng làm việc tối thiểu
        defaultConfig.setMaxWorkingHours(12.0);    // 12 tiếng làm việc tối đa

        // Mặc định làm việc từ thứ 2 đến thứ 6
        defaultConfig.setMonday(true);
        defaultConfig.setTuesday(true);
        defaultConfig.setWednesday(true);
        defaultConfig.setThursday(true);
        defaultConfig.setFriday(true);
        defaultConfig.setSaturday(true);
        defaultConfig.setSunday(false);

        return defaultConfig;
    }

    // Phương thức kiểm tra khoảng cách GPS với vị trí công ty
    private boolean isWithinAllowedDistance(Double latitude, Double longitude,
                                            Double companyLat, Double companyLng, double maxDistanceInMeters) {
        // Công thức Haversine để tính khoảng cách
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(companyLat - latitude);
        double dLng = Math.toRadians(companyLng - longitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(companyLat)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        return distance <= maxDistanceInMeters;
    }

    // Lấy lịch sử chấm công của nhân viên
    public List<AttendanceDTO> getUserAttendanceHistory(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        return attendanceRepository
                .findByUserIdAndDateRange(userId, startDate, endDate, (Pageable) pageable)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Thống kê tổng quan theo phòng ban
    public DepartmentStatsDTO getDepartmentStats(Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Attendance> attendances = attendanceRepository
                .findByDepartmentAndDateRange(departmentId, startDate, endDate);

        DepartmentStatsDTO stats = new DepartmentStatsDTO();

        // Đếm theo trạng thái sử dụng enum
        stats.setOnLeaveCount(countByStatus(attendances, AttendanceStatus.ON_LEAVE));
        stats.setTotalAttendance((long) attendances.size());
        stats.setOnTimeCount(countByStatus(attendances, AttendanceStatus.PRESENT));
        stats.setLateCount(countByStatus(attendances, AttendanceStatus.LATE));
        stats.setAbsentCount(countByStatus(attendances, AttendanceStatus.ABSENT));

        // Đếm số người làm thêm giờ
        stats.setOvertimeCount(attendances.stream()
                .filter(Attendance::getIsOvertime)
                .count());

        // Tính tổng giờ làm việc
        double totalWorkingHours = attendances.stream()
                .mapToDouble(Attendance::getWorkingHours)
                .sum();
        stats.setTotalWorkingHours(totalWorkingHours);

        return stats;
    }

    // Thống kê theo từng nhân viên trong phòng ban
    public List<UserAttendanceStatsDTO> getUsersAttendanceStats(
            Long departmentId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return attendanceRepository.findUserStatsInDepartment(departmentId, startDate, endDate);
    }

    // Thống kê làm thêm giờ
    public List<OvertimeStatsDTO> getOvertimeStats(
            Long departmentId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<OvertimeStatsDTO> stats = attendanceRepository.findOvertimeStats(
                departmentId, startDate, endDate);

        // Calculate overtime days for each employee
        for (OvertimeStatsDTO stat : stats) {
            Integer overtimeDays = attendanceRepository.countOvertimeDays(
                    stat.getUserId(), startDate, endDate);
            stat.setOvertimeDays(overtimeDays);
        }

        return stats;
    }

    // Xuất báo cáo Excel
    public byte[] exportAttendanceReport(
            Long departmentId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        List<Attendance> attendances = attendanceRepository
                .findByDepartmentAndDateRange(departmentId, startDate, endDate);

        return generateExcelReport(attendances);
    }

    private AttendanceDTO convertToDTO(Attendance attendance) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(attendance.getId());
        dto.setUserId(attendance.getUser().getId());
        dto.setUserName(attendance.getUser().getName());
        dto.setCheckInTime(attendance.getCheckInTime());
        dto.setCheckOutTime(attendance.getCheckOutTime());
        AttendanceStatus status = AttendanceStatus.valueOf(attendance.getStatus());
        dto.setStatus(status != null ? AttendanceStatus.valueOf(status.name()) : null);
        dto.setWorkingHours(attendance.getWorkingHours());
        dto.setIsOvertime(attendance.getIsOvertime());
        return dto;
    }
    // Thêm phương thức generateExcelReport
    private byte[] generateExcelReport(List<Attendance> attendances) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance Report");

            // Tạo header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"User ID", "Name", "Check In Time", "Check Out Time",
                    "Status", "Working Hours", "Overtime"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Điền dữ liệu
            int rowNum = 1;
            for (Attendance attendance : attendances) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(attendance.getUser().getId());
                row.createCell(1).setCellValue(attendance.getUser().getName());
                row.createCell(2).setCellValue(attendance.getCheckInTime().toString());
                row.createCell(3).setCellValue(attendance.getCheckOutTime() != null ?
                        attendance.getCheckOutTime().toString() : "");
                row.createCell(4).setCellValue(attendance.getStatus());
                row.createCell(5).setCellValue(attendance.getWorkingHours());
                row.createCell(6).setCellValue(attendance.getIsOvertime());
            }

            // Auto size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Chuyển workbook thành byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    // Thêm phương thức để kiểm tra nếu một ngày là ngày nghỉ
    private boolean isHoliday(LocalDateTime date) {
        // TODO: Implement holiday checking logic
        return false;
    }

    // Thêm phương thức để tính overtime
    private boolean calculateOvertime(LocalDateTime checkOutTime) {
        LocalTime endTime = checkOutTime.toLocalTime();
        return endTime.isAfter(LocalTime.of(17, 30)); // Sau 5:30 chiều
    }

    // Xem lịch sử chi tiết theo nhiều tiêu chí
    public List<AttendanceDTO> searchAttendanceHistory(AttendanceSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        return attendanceRepository.findBySearchCriteria(
                        request.getUserId(),
                        request.getDepartmentId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getStatus(), // AttendanceStatus
                        pageable
                )
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    // Thống kê tổng hợp cho một user
    public UserAttendanceSummaryDTO getUserAttendanceSummary(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        List<Attendance> attendances = attendanceRepository
                .findByUserIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(
                        userId, startDate, endDate);

        return UserAttendanceSummaryDTO.builder()
                .totalDays(attendances.size())
                .presentDays((int) countByStatus(attendances, AttendanceStatus.PRESENT))
                .lateDays((int) countByStatus(attendances, AttendanceStatus.LATE))
                .absentDays((int) countByStatus(attendances, AttendanceStatus.ABSENT))
                .totalWorkingHours(calculateTotalWorkingHours(attendances))
                .averageWorkingHours(calculateAverageWorkingHours(attendances))
                .overtimeDays((int) countOvertimeDays(attendances))
                .build();
    }

    // Chức năng cho quản lý: Chỉnh sửa chấm công
//    @PreAuthorize("hasRole('MANAGER')")
    public Attendance updateAttendance(Long attendanceId, AttendanceUpdateRequest updateRequest) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        // Lưu lại thông tin chỉnh sửa
        AttendanceAuditLog auditLog = new AttendanceAuditLog();
        auditLog.setAttendance(attendance);
        auditLog.setModifiedBy(updateRequest.getModifiedBy());
        auditLog.setModificationTime(LocalDateTime.now());
        auditLog.setOldValue(attendance.toString());

        // Cập nhật thông tin chấm công
        if (updateRequest.getCheckInTime() != null) {
            attendance.setCheckInTime(updateRequest.getCheckInTime());
        }
        if (updateRequest.getCheckOutTime() != null) {
            attendance.setCheckOutTime(updateRequest.getCheckOutTime());
        }
        if (updateRequest.getStatus() != null) {
            attendance.setStatus(String.valueOf(updateRequest.getStatus()));
        }

        // Tính toán lại số giờ làm việc
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            Duration duration = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime());
            attendance.setWorkingHours(duration.toMinutes() / 60.0);
            attendance.setIsOvertime(calculateOvertime(attendance.getCheckOutTime()));
        }

        attendance.setLastModifiedBy(updateRequest.getModifiedBy());
        attendance.setLastModifiedTime(LocalDateTime.now());
        attendance.setModificationReason(updateRequest.getReason());

        auditLog.setNewValue(attendance.toString());
        attendanceAuditLogRepository.save(auditLog);

        return attendanceRepository.save(attendance);
    }

    // Phê duyệt yêu cầu sửa chấm công
//    @PreAuthorize("hasRole('MANAGER')")
    public AttendanceModificationRequest approveModificationRequest(
            Long requestId,
            String approverId,
            boolean approved,
            String comment) {
        AttendanceModificationRequest request = modificationRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new RuntimeException("Modification request not found"));

        request.setApproved(approved);
        request.setApprovedBy(approverId);
        request.setApprovalTime(LocalDateTime.now());
        request.setApprovalComment(comment);

        if (approved) {
            // Cập nhật chấm công theo yêu cầu
            Attendance attendance = request.getAttendance();
            attendance.setCheckInTime(request.getRequestedCheckInTime());
            attendance.setCheckOutTime(request.getRequestedCheckOutTime());
            attendance.setLastModifiedBy(approverId);
            attendance.setLastModifiedTime(LocalDateTime.now());
            attendance.setModificationReason("Approved modification request: " + request.getReason());
            attendanceRepository.save(attendance);
        }

        return modificationRequestRepository.save(request);
    }

    // Các phương thức hỗ trợ
    private long countByStatus(List<Attendance> attendances, AttendanceStatus status) {
        return attendances.stream()
                .filter(a -> status.equals(a.getStatus()))
                .count();
    }

    private double calculateTotalWorkingHours(List<Attendance> attendances) {
        return attendances.stream()
                // Filter out null workingHours
                .filter(a -> a.getWorkingHours() != null)
                .mapToDouble(Attendance::getWorkingHours)
                .sum();
    }

    private double calculateAverageWorkingHours(List<Attendance> attendances) {
        return attendances.stream()
                .filter(a -> a.getWorkingHours() != null)  // Filter out null values
                .mapToDouble(Attendance::getWorkingHours)
                .average()
                .orElse(0.0);
    }

    private long countOvertimeDays(List<Attendance> attendances) {
        return attendances.stream()
                .filter(Attendance::getIsOvertime)
                .count();
    }

    // Tạo yêu cầu chỉnh sửa chấm công
    public AttendanceModificationRequest createModificationRequest(AttendanceUpdateRequest request) {
        User requester = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Attendance attendance = attendanceRepository.findById(request.getAttendanceId())
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));

        AttendanceModificationRequest modRequest = new AttendanceModificationRequest();
        modRequest.setAttendance(attendance);
        modRequest.setRequestedBy(requester);
        modRequest.setRequestTime(LocalDateTime.now());
        modRequest.setRequestedCheckInTime(request.getCheckInTime());
        modRequest.setRequestedCheckOutTime(request.getCheckOutTime());
        modRequest.setReason(request.getReason());
        modRequest.setApproved(false);

        return modificationRequestRepository.save(modRequest);
    }

    // Lấy các yêu cầu chỉnh sửa đang chờ duyệt
    public List<AttendanceModificationRequest> getPendingModificationRequests(Long departmentId) {
        return modificationRequestRepository.findByDepartmentIdAndApprovedFalse(departmentId);
    }

    // Lấy danh sách yêu cầu chỉnh sửa của user
    public List<AttendanceModificationRequest> getUserModificationRequests(String userId) {
        return modificationRequestRepository.findByRequestedByOrderByRequestTimeDesc(userId);
    }

    // Lấy lịch sử chỉnh sửa của một bản ghi chấm công
    public List<AttendanceAuditLog> getAttendanceAuditLog(Long attendanceId) {
        return attendanceAuditLogRepository.findByAttendanceIdOrderByModificationTimeDesc(attendanceId);
    }

    // Thống kê chấm công theo ngày
    public DailyAttendanceStatsDTO getDailyAttendanceStats(Long departmentId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);

        List<Attendance> attendances = attendanceRepository
                .findByDepartmentAndDateRange(departmentId, startOfDay, endOfDay);

        return DailyAttendanceStatsDTO.builder()
                .totalEmployees(countUniqueUsers(attendances))
                .presentCount(countByStatus(attendances, AttendanceStatus.valueOf("PRESENT")))
                .lateCount(countByStatus(attendances, AttendanceStatus.valueOf("LATE")))
                .absentCount(countByStatus(attendances, AttendanceStatus.valueOf("ABSENT")))
                .overtimeCount(countOvertimeDays(attendances))
                .averageArrivalTime(calculateAverageArrivalTime(attendances))
                .averageDepartureTime(calculateAverageDepartureTime(attendances))
                .build();
    }

    // Các phương thức hỗ trợ
    private long countUniqueUsers(List<Attendance> attendances) {
        return attendances.stream()
                .map(a -> a.getUser().getId())
                .distinct()
                .count();
    }

    private LocalTime calculateAverageArrivalTime(List<Attendance> attendances) {
        OptionalDouble avgMinutes = attendances.stream()
                .map(a -> a.getCheckInTime().toLocalTime())
                .mapToDouble(t -> t.getHour() * 60 + t.getMinute())
                .average();

        if (avgMinutes.isPresent()) {
            int minutes = (int) avgMinutes.getAsDouble();
            return LocalTime.of(minutes / 60, minutes % 60);
        }
        return null;
    }

    private LocalTime calculateAverageDepartureTime(List<Attendance> attendances) {
        OptionalDouble avgMinutes = attendances.stream()
                .filter(a -> a.getCheckOutTime() != null)
                .map(a -> a.getCheckOutTime().toLocalTime())
                .mapToDouble(t -> t.getHour() * 60 + t.getMinute())
                .average();

        if (avgMinutes.isPresent()) {
            int minutes = (int) avgMinutes.getAsDouble();
            return LocalTime.of(minutes / 60, minutes % 60);
        }
        return null;
    }

    public Attendance getAttendanceByDate(String userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return attendanceRepository.findFirstByUserIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(
                userId,
                startOfDay,
                endOfDay
        ).orElse(null);
    }
}