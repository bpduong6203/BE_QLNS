package DA.backend.service;

import DA.backend.dto.DepartmentLeaveStatsDTO;
import DA.backend.dto.RemainingLeaveDTO;
import DA.backend.entity.Department;
import DA.backend.entity.Leave;
import DA.backend.entity.User;
import DA.backend.entity.UserLeaveInfo;
import DA.backend.enums.LeaveStatus;
import DA.backend.enums.LeaveType;
import DA.backend.repository.LeaveRepository;
import DA.backend.repository.UserDepartmentRepository;
import DA.backend.repository.UserLeaveInfoRepository;
import DA.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    UserLeaveInfoRepository userLeaveInfoRepository;

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;

    // Tạo đơn xin nghỉ
    public Leave createLeave(Leave leave) {
        // Validate thời gian nghỉ
        validateLeaveRequest(leave);

        // Lưu đơn xin nghỉ
        Leave savedLeave = leaveRepository.save(leave);

        // Gửi email thông báo cho quản lý
        //notifyManager(savedLeave);

        return savedLeave;
    }
    public Leave createLeaveWithEvidence(Leave leave, MultipartFile evidenceFile) {
        try {
            String userId = leave.getUser().getId();
            log.info("Creating leave request for user: {}", userId);

            // Lấy user với department từ database
            User user = userRepository.findByIdWithDepartments(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Force initialize department
            Set<Department> departments = user.getDepartment();
            Hibernate.initialize(departments);

            if (departments == null || departments.isEmpty()) {
                log.error("No department found for user: {}", userId);
                throw new RuntimeException("User must be assigned to a department before creating leave requests");
            }

            Department department = departments.iterator().next();
            log.info("Found department: {}", department.getName());

            // Set department và user cho leave request
            leave.setUser(user);
            leave.setDepartment(department);

            // Các xử lý khác...
            if (evidenceFile != null && !evidenceFile.isEmpty()) {
                String base64Image = Base64.getEncoder().encodeToString(evidenceFile.getBytes());
                leave.setEvidenceImage(base64Image);
            }

            leave.setStatus(LeaveStatus.PENDING);
            leave.setCreatedAt(LocalDateTime.now());

            return leaveRepository.save(leave);
        } catch (Exception e) {
            log.error("Error creating leave request", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    // Thêm phương thức lấy hình ảnh minh chứng
    public String getLeaveEvidence(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        return leave.getEvidenceImage();
    }

    // Xử lý phê duyệt đơn
    public Leave approveLeave(Long leaveId, String approverId, boolean approved, String comment) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        leave.setStatus(approved ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        leave.setApprovedBy(approverId);
        leave.setApprovedAt(LocalDateTime.now());
        leave.setComment(comment);

        Leave updatedLeave = leaveRepository.save(leave);

        // Tự động cập nhật số ngày nghỉ nếu đơn được duyệt
        if (approved) {
            updateRemainingLeaveDays(leave);
        }

        // Gửi email thông báo kết quả cho nhân viên
        notifyEmployee(updatedLeave);

        return updatedLeave;
    }

    private void updateRemainingLeaveDays(Leave leave) {
        UserLeaveInfo leaveInfo = userLeaveInfoRepository.findByUserId(leave.getUser().getId())
                .orElseGet(() -> {
                    UserLeaveInfo newInfo = new UserLeaveInfo();
                    newInfo.setUser(leave.getUser());
                    newInfo.setAnnualLeaveDays(12); // Số ngày nghỉ mặc định
                    return newInfo;
                });

        // Tính số ngày nghỉ trong đơn hiện tại
        double leaveDays = calculateDays(leave);

        // Cập nhật số ngày nghỉ còn lại theo loại nghỉ
        switch (leave.getType()) {
            case ANNUAL_LEAVE:
                int remainingAnnual = leaveInfo.getAnnualLeaveDays() - (int) leaveDays;
                leaveInfo.setAnnualLeaveDays(Math.max(0, remainingAnnual));
                break;
            case SICK_LEAVE:
                int remainingSick = leaveInfo.getSickLeaveDays() - (int) leaveDays;
                leaveInfo.setSickLeaveDays(Math.max(0, remainingSick));
                break;
            // Có thể thêm các loại nghỉ khác
        }

        leaveInfo.setUpdatedAt(LocalDateTime.now());
        userLeaveInfoRepository.save(leaveInfo);
    }
    @Scheduled(cron = "0 0 0 1 1 *") // Chạy vào ngày 1/1 hàng năm
    public void resetAnnualLeaveDays() {
        List<UserLeaveInfo> allUserLeaveInfos = userLeaveInfoRepository.findAll();

        for (UserLeaveInfo leaveInfo : allUserLeaveInfos) {
            // Cộng dồn số ngày nghỉ còn lại (nếu cho phép)
            int remainingDays = leaveInfo.getAnnualLeaveDays();
            int maxCarryOver = 5; // Số ngày tối đa được phép cộng dồn
            int carryOver = Math.min(remainingDays, maxCarryOver);

            // Reset và cộng số ngày mới
            leaveInfo.setAnnualLeaveDays(12 + carryOver); // 12 ngày nghỉ mới + số ngày cộng dồn
            leaveInfo.setUpdatedAt(LocalDateTime.now());
        }

        userLeaveInfoRepository.saveAll(allUserLeaveInfos);
    }

    // Kiểm tra user có đang nghỉ phép không
    public boolean isUserOnLeave(String userId, LocalDateTime date) {
        return leaveRepository.isUserOnLeave(userId, date);
    }

    private void validateLeaveRequest(Leave leave) {
        // Kiểm tra số ngày nghỉ còn lại
        // Kiểm tra thời gian nghỉ hợp lệ
        // Kiểm tra trùng lặp với đơn nghỉ khác
    }

//    private void notifyManager(Leave leave) {
//        if (leave == null || leave.getUser() == null) {
//            log.warn("Leave or user is null");
//            return;
//        }
//
//        // Lấy department từ leave
//        Department department = leave.getDepartment();
//        if (department == null) {
//            log.warn("Leave request has no department assigned");
//            return;
//        }
//
//        // Lấy manager từ department
//        User manager = department.getManager();
//        if (manager == null || manager.getEmail() == null) {
//            log.warn("No manager or manager email found for department: {}", department.getName());
//            return;
//        }
//
//        String subject = "Có đơn xin nghỉ phép mới cần duyệt";
//        String body = String.format(
//                "Xin chào,\n\n" +
//                        "Có đơn xin nghỉ phép mới cần được phê duyệt. Chi tiết như sau:\n\n" +
//                        "- Nhân viên: %s\n" +
//                        "- Phòng ban: %s\n" +
//                        "- Loại nghỉ phép: %s\n" +
//                        "- Thời gian: từ %s đến %s\n" +
//                        "- Số ngày nghỉ: %.1f ngày\n" +
//                        "- Lý do: %s\n\n" +
//                        "Vui lòng đăng nhập vào hệ thống để xem xét và phê duyệt.\n\n" +
//                        "Trân trọng,\n" +
//                        "Hệ thống quản lý nghỉ phép",
//                leave.getUser().getName(),
//                department.getName(),
//                leave.getType().getDescription(),
//                formatDateTime(leave.getStartDate()),
//                formatDateTime(leave.getEndDate()),
//                calculateDays(leave),
//                leave.getReason()
//        );
//
//        try {
//            emailService.sendEmail(manager.getEmail(), subject, body);
//            log.info("Sent notification email to manager: {} for leave request from: {}",
//                    manager.getEmail(), leave.getUser().getName());
//        } catch (Exception e) {
//            log.error("Failed to send email to manager: {}", e.getMessage());
//            // Không throw exception để không ảnh hưởng đến việc tạo leave
//        }
//    }

    private void notifyEmployee(Leave leave) {
        String to = leave.getUser().getEmail();
        String subject;
        String body;

        switch (leave.getStatus()) {
            case APPROVED:
                subject = "Đơn xin nghỉ phép đã được phê duyệt";
                body = String.format(
                        "Xin chào %s,\n\n" +
                                "Đơn xin nghỉ phép của bạn đã được phê duyệt. Chi tiết như sau:\n\n" +
                                "- Loại nghỉ phép: %s\n" +
                                "- Thời gian: từ %s đến %s\n" +
                                "- Số ngày nghỉ: %.1f ngày\n" +
                                "- Người phê duyệt: %s\n" +
                                "- Ghi chú: %s\n\n" +
                                "Vui lòng kiểm tra lại số ngày nghỉ còn lại của bạn trong hệ thống.\n\n" +
                                "Trân trọng,\n" +
                                "Phòng Nhân sự",
                        leave.getUser().getName(),
                        leave.getType().getDescription(),
                        formatDateTime(leave.getStartDate()),
                        formatDateTime(leave.getEndDate()),
                        calculateDays(leave),
                        leave.getApprovedBy(),
                        leave.getComment() != null ? leave.getComment() : "Không có"
                );
                break;

            case REJECTED:
                subject = "Đơn xin nghỉ phép không được phê duyệt";
                body = String.format(
                        "Xin chào %s,\n\n" +
                                "Đơn xin nghỉ phép của bạn không được phê duyệt. Chi tiết như sau:\n\n" +
                                "- Loại nghỉ phép: %s\n" +
                                "- Thời gian đã yêu cầu: từ %s đến %s\n" +
                                "- Lý do từ chối: %s\n\n" +
                                "Vui lòng liên hệ quản lý trực tiếp nếu bạn cần thêm thông tin.\n\n" +
                                "Trân trọng,\n" +
                                "Phòng Nhân sự",
                        leave.getUser().getName(),
                        leave.getType().getDescription(),
                        formatDateTime(leave.getStartDate()),
                        formatDateTime(leave.getEndDate()),
                        leave.getComment() != null ? leave.getComment() : "Không có lý do cụ thể"
                );
                break;

            case REVOKED:
                subject = "Thông báo hủy phê duyệt đơn xin nghỉ phép";
                body = String.format(
                        "Xin chào %s,\n\n" +
                                "Đơn xin nghỉ phép của bạn đã bị hủy phê duyệt. Chi tiết như sau:\n\n" +
                                "- Loại nghỉ phép: %s\n" +
                                "- Thời gian: từ %s đến %s\n" +
                                "- Người hủy duyệt: %s\n" +
                                "- Lý do hủy: %s\n\n" +
                                "Số ngày nghỉ đã được hoàn lại vào tài khoản của bạn.\n" +
                                "Vui lòng liên hệ quản lý trực tiếp nếu bạn cần thêm thông tin.\n\n" +
                                "Trân trọng,\n" +
                                "Phòng Nhân sự",
                        leave.getUser().getName(),
                        leave.getType().getDescription(),
                        formatDateTime(leave.getStartDate()),
                        formatDateTime(leave.getEndDate()),
                        leave.getApprovedBy(),
                        leave.getComment() != null ? leave.getComment() : "Không có lý do cụ thể"
                );
                break;

            case CANCELLED:
                subject = "Xác nhận hủy đơn xin nghỉ phép";
                body = String.format(
                        "Xin chào %s,\n\n" +
                                "Đơn xin nghỉ phép của bạn đã được hủy theo yêu cầu. Chi tiết như sau:\n\n" +
                                "- Loại nghỉ phép: %s\n" +
                                "- Thời gian đã yêu cầu: từ %s đến %s\n\n" +
                                "Trân trọng,\n" +
                                "Phòng Nhân sự",
                        leave.getUser().getName(),
                        leave.getType().getDescription(),
                        formatDateTime(leave.getStartDate()),
                        formatDateTime(leave.getEndDate())
                );
                break;

            default:
                return; // Không gửi email cho các trạng thái khác
        }

        // Gửi email thông báo
        emailService.sendEmail(to, subject, body);
    }

    public List<Leave> getLeavesByUserId(String userId) {
        try {
            User user = userService.checkUser(userId);
            if (user == null) {
                throw new RuntimeException("User not found: " + userId);
            }
            return leaveRepository.findByUserIdOrderByCreatedAtDesc(userId);
        } catch (Exception e) {
            System.err.println("Error getting leaves for user " + userId + ": " + e.getMessage());
            return new ArrayList<>(); // Trả về list rỗng thay vì throw exception
        }
    }

    public Leave cancelLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Can only cancel pending requests");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        return leaveRepository.save(leave);
    }

    public RemainingLeaveDTO getRemainingLeaveDays(String userId) {
        try {
            User user = userService.checkUser(userId);
            if (user == null) {
                System.err.println("User not found when getting remaining days: " + userId);
                return getDefaultRemainingDTO(); // Trả về giá trị mặc định
            }

            // Lấy thông tin nghỉ phép từ database
            UserLeaveInfo leaveInfo = userLeaveInfoRepository.findByUserId(userId)
                    .orElse(new UserLeaveInfo());

            int annualLeaveTotal = leaveInfo.getAnnualLeaveDays();
            if (annualLeaveTotal == 0) annualLeaveTotal = 12; // Default value

            int sickLeaveTotal = 30; // Default value

            // Tính số ngày đã nghỉ thực tế từ database
            int annualLeaveUsed = calculateUsedLeaveDays(userId, LeaveType.ANNUAL_LEAVE);
            int sickLeaveUsed = calculateUsedLeaveDays(userId, LeaveType.SICK_LEAVE);
            int pendingRequests = countPendingRequests(userId);

            return RemainingLeaveDTO.builder()
                    .annualLeaveRemaining(annualLeaveTotal - annualLeaveUsed)
                    .sickLeaveRemaining(sickLeaveTotal - sickLeaveUsed)
                    .unpaidLeaveRemaining(Integer.MAX_VALUE)
                    .totalLeaveTaken(annualLeaveUsed + sickLeaveUsed)
                    .pendingRequests(pendingRequests)
                    .build();
        } catch (Exception e) {
            System.err.println("Error calculating remaining leave days for user " + userId + ": " + e.getMessage());
            return getDefaultRemainingDTO();
        }
    }
    private RemainingLeaveDTO getDefaultRemainingDTO() {
        return RemainingLeaveDTO.builder()
                .annualLeaveRemaining(0)
                .sickLeaveRemaining(0)
                .unpaidLeaveRemaining(0)
                .totalLeaveTaken(0)
                .pendingRequests(0)
                .build();
    }

    public List<Leave> getPendingLeavesByDepartment(Long departmentId) {
        return leaveRepository.findByDepartmentAndStatus(departmentId, LeaveStatus.PENDING);
    }

    private int calculateUsedLeaveDays(String userId, LeaveType type) {
        try {
            List<Leave> userLeaves = leaveRepository.findByUserIdOrderByCreatedAtDesc(userId);
            return (int) userLeaves.stream()
                    .filter(leave -> leave.getType() == type)
                    .filter(leave -> leave.getStatus() == LeaveStatus.APPROVED)
                    .mapToDouble(this::calculateDays)
                    .sum();
        } catch (Exception e) {
            System.err.println("Error calculating used leave days: " + e.getMessage());
            return 0;
        }
    }

    private int countPendingRequests(String userId) {
        try {
            List<Leave> userLeaves = leaveRepository.findByUserIdOrderByCreatedAtDesc(userId);
            return (int) userLeaves.stream()
                    .filter(leave -> leave.getStatus() == LeaveStatus.PENDING)
                    .count();
        } catch (Exception e) {
            System.err.println("Error counting pending requests: " + e.getMessage());
            return 0;
        }
    }

    // Lấy thống kê nghỉ phép theo phòng ban
    public DepartmentLeaveStatsDTO getDepartmentLeaveStats(
            Long departmentId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<Leave> leaves = leaveRepository.findByDepartmentAndDateRange(
                departmentId, startDate, endDate);

        return DepartmentLeaveStatsDTO.builder()
                .departmentId(departmentId)
                .totalEmployees(countUniqueEmployees(leaves))
                .onLeaveCount(countByStatus(leaves, LeaveStatus.APPROVED))
                .pendingCount(countByStatus(leaves, LeaveStatus.PENDING))
                .totalLeaveDays(calculateTotalLeaveDays(leaves))
                .averageLeaveDays(calculateAverageLeaveDays(leaves))
                .build();
    }

    // Xuất báo cáo nghỉ phép
    public byte[] exportLeaveReport(
            Long departmentId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<Leave> leaves = leaveRepository.findByDepartmentAndDateRange(
                departmentId, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Leave Report");

            // Tạo header
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Employee ID", "Name", "Leave Type", "Start Date",
                    "End Date", "Days", "Status", "Reason"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Điền dữ liệu
            int rowNum = 1;
            for (Leave leave : leaves) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(leave.getUser().getId());
                row.createCell(1).setCellValue(leave.getUser().getName());
                row.createCell(2).setCellValue(leave.getType().getDescription());
                row.createCell(3).setCellValue(formatDateTime(leave.getStartDate()));
                row.createCell(4).setCellValue(formatDateTime(leave.getEndDate()));
                row.createCell(5).setCellValue(calculateDays(leave));
                row.createCell(6).setCellValue(leave.getStatus().getDescription());
                row.createCell(7).setCellValue(leave.getReason());
            }

            // Auto size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate leave report", e);
        }
    }

    // Cập nhật số ngày nghỉ phép năm
    public void updateAnnualLeaveDays(String userId, int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Number of days cannot be negative");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Giả sử có một bảng lưu thông tin nghỉ phép của user
        UserLeaveInfo leaveInfo = userLeaveInfoRepository.findByUserId(userId)
                .orElse(new UserLeaveInfo());

        leaveInfo.setUser(user);
        leaveInfo.setAnnualLeaveDays(days);
        leaveInfo.setUpdatedAt(LocalDateTime.now());

        userLeaveInfoRepository.save(leaveInfo);
    }

    // Các phương thức helper
    private long countUniqueEmployees(List<Leave> leaves) {
        return leaves.stream()
                .map(leave -> leave.getUser().getId())
                .distinct()
                .count();
    }

    private long countByStatus(List<Leave> leaves, LeaveStatus status) {
        return leaves.stream()
                .filter(leave -> status.equals(leave.getStatus()))
                .count();
    }

    private double calculateTotalLeaveDays(List<Leave> leaves) {
        return leaves.stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.APPROVED)
                .mapToDouble(this::calculateDays)
                .sum();
    }

    private double calculateAverageLeaveDays(List<Leave> leaves) {
        long employeeCount = countUniqueEmployees(leaves);
        return employeeCount > 0 ? calculateTotalLeaveDays(leaves) / employeeCount : 0;
    }

    private double calculateDays(Leave leave) {
        return ChronoUnit.DAYS.between(
                leave.getStartDate().toLocalDate(),
                leave.getEndDate().toLocalDate()
        ) + 1;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    // Lấy danh sách đơn xin nghỉ theo khoảng thời gian và trạng thái
    public List<Leave> getLeavesByDateRange(Long departmentId, LocalDateTime startDate, LocalDateTime endDate, LeaveStatus status) {
        if (status != null) {
            return leaveRepository.findByDepartmentAndDateRangeAndStatus(departmentId, startDate, endDate, status);
        }
        return leaveRepository.findByDepartmentAndDateRange(departmentId, startDate, endDate);
    }

    // Lấy chi tiết đơn xin nghỉ theo ID
    public Leave getLeaveById(Long leaveId) {
        return leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
    }

    // Thống kê số đơn theo từng loại nghỉ
    public Map<LeaveType, Integer> getLeaveStatsByType(Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Leave> leaves = leaveRepository.findByDepartmentAndDateRange(departmentId, startDate, endDate);
        return leaves.stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.APPROVED)
                .collect(Collectors.groupingBy(
                        Leave::getType,
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Long::intValue
                        )
                ));
    }

    // Lấy danh sách nhân viên đang nghỉ
    public List<Leave> getCurrentLeaves(Long departmentId) {
        LocalDateTime now = LocalDateTime.now();
        return leaveRepository.findByDepartmentAndDateRange(departmentId, now, now)
                .stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.APPROVED)
                .collect(Collectors.toList());
    }

    // Hủy duyệt đơn xin nghỉ
    public Leave revokeLeaveApproval(Long leaveId, String managerId, String reason) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        if (leave.getStatus() != LeaveStatus.APPROVED) {
            throw new RuntimeException("Can only revoke approved leaves");
        }

        // Hoàn lại số ngày nghỉ đã trừ
        restoreRemainingLeaveDays(leave);

        // Cập nhật trạng thái đơn
        leave.setStatus(LeaveStatus.REVOKED);
        leave.setComment(reason);
        leave.setApprovedBy(managerId);
        leave.setApprovedAt(LocalDateTime.now());

        Leave updatedLeave = leaveRepository.save(leave);
        notifyEmployee(updatedLeave); // Thông báo cho nhân viên

        return updatedLeave;
    }

    // Hoàn lại số ngày nghỉ khi hủy duyệt
    private void restoreRemainingLeaveDays(Leave leave) {
        UserLeaveInfo leaveInfo = userLeaveInfoRepository.findByUserId(leave.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User leave info not found"));

        double leaveDays = calculateDays(leave);

        switch (leave.getType()) {
            case ANNUAL_LEAVE:
                leaveInfo.setAnnualLeaveDays(leaveInfo.getAnnualLeaveDays() + (int) leaveDays);
                break;
            case SICK_LEAVE:
                leaveInfo.setSickLeaveDays(leaveInfo.getSickLeaveDays() + (int) leaveDays);
                break;
        }

        leaveInfo.setUpdatedAt(LocalDateTime.now());
        userLeaveInfoRepository.save(leaveInfo);
    }

    // Xuất báo cáo theo nhân viên
    public byte[] exportLeaveReportByEmployee(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Leave> leaves = leaveRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employee Leave Report");

            // Tạo header
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Leave Type", "Start Date", "End Date", "Days",
                    "Status", "Reason", "Approved By", "Approved At"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Điền dữ liệu
            int rowNum = 1;
            for (Leave leave : leaves) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(leave.getType().getDescription());
                row.createCell(1).setCellValue(formatDateTime(leave.getStartDate()));
                row.createCell(2).setCellValue(formatDateTime(leave.getEndDate()));
                row.createCell(3).setCellValue(calculateDays(leave));
                row.createCell(4).setCellValue(leave.getStatus().getDescription());
                row.createCell(5).setCellValue(leave.getReason());
                row.createCell(6).setCellValue(leave.getApprovedBy());
                row.createCell(7).setCellValue(leave.getApprovedAt() != null ?
                        formatDateTime(leave.getApprovedAt()) : "");
            }

            // Auto size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate employee leave report", e);
        }
    }




}