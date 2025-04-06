package DA.backend.controller;

import DA.backend.dto.*;
import DA.backend.entity.Attendance;
import DA.backend.entity.AttendanceAuditLog;
import DA.backend.entity.AttendanceModificationRequest;
import DA.backend.enums.AttendanceStatus;
import DA.backend.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendance/manager")
@CrossOrigin
public class AttendanceManagerController {

    @Autowired
    private AttendanceService attendanceService;

    // Lấy thống kê theo phòng ban
    @GetMapping("/department/stats")
    public ResponseEntity<DepartmentStatsDTO> getDepartmentStats(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(attendanceService.getDepartmentStats(departmentId, startDate, endDate));
    }

    // Lấy thống kê theo từng nhân viên
    @GetMapping("/users/stats")
    public ResponseEntity<Object> getUsersStats(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(attendanceService.getUsersAttendanceStats(departmentId, startDate, endDate));
    }

    // Thống kê overtime
    @GetMapping("/overtime/stats")
    public ResponseEntity<List<OvertimeStatsDTO>> getOvertimeStats(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(attendanceService.getOvertimeStats(departmentId, startDate, endDate));
    }

    // Xuất báo cáo Excel
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        byte[] report = attendanceService.exportAttendanceReport(departmentId, startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(report);
    }

    // Tìm kiếm chấm công theo nhiều tiêu chí
    @GetMapping("/search")
    public ResponseEntity<List<AttendanceDTO>> searchAttendance(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) AttendanceStatus status,  // Đổi String thành AttendanceStatus
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        AttendanceSearchRequest request = new AttendanceSearchRequest();
        request.setUserId(userId);
        request.setDepartmentId(departmentId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setStatus(status);  // Không cần chuyển đổi vì đã là enum
        request.setPage(page);
        request.setSize(size);

        return ResponseEntity.ok(attendanceService.searchAttendanceHistory(request));
    }

    // Xem danh sách yêu cầu chỉnh sửa
    @GetMapping("/modification-requests/pending")
    public ResponseEntity<List<AttendanceModificationRequest>> getPendingRequests(
            @RequestParam Long departmentId) {
        return ResponseEntity.ok(attendanceService.getPendingModificationRequests(departmentId));
    }

    // Phê duyệt yêu cầu chỉnh sửa
    @PostMapping("/modification-requests/{requestId}/approve")
    public ResponseEntity<AttendanceModificationRequest> approveRequest(
            @PathVariable Long requestId,
            @RequestParam String approverId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(attendanceService.approveModificationRequest(
                requestId, approverId, approved, comment));
    }

    // Chỉnh sửa trực tiếp chấm công
    @PutMapping("/{attendanceId}")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable Long attendanceId,
            @RequestBody AttendanceUpdateRequest updateRequest) {
        try {
            return ResponseEntity.ok(attendanceService.updateAttendance(attendanceId, updateRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Xem lịch sử chỉnh sửa
    @GetMapping("/{attendanceId}/audit-log")
    public ResponseEntity<List<AttendanceAuditLog>> getAuditLog(
            @PathVariable Long attendanceId) {
        return ResponseEntity.ok(attendanceService.getAttendanceAuditLog(attendanceId));
    }

    // Thống kê chi tiết theo ngày
    @GetMapping("/daily-stats")
    public ResponseEntity<DailyAttendanceStatsDTO> getDailyStats(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(attendanceService.getDailyAttendanceStats(departmentId, date));
    }
}