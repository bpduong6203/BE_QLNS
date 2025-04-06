package DA.backend.controller;

import DA.backend.dto.AttendanceDTO;
import DA.backend.dto.AttendanceSearchRequest;
import DA.backend.dto.AttendanceUpdateRequest;
import DA.backend.dto.UserAttendanceSummaryDTO;
import DA.backend.entity.Attendance;
import DA.backend.entity.AttendanceModificationRequest;
import DA.backend.enums.AttendanceCodeType;
import DA.backend.service.AttendanceCodeService;
import DA.backend.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceCodeService attendanceCodeService;

    // API tạo mã QR/code mới
    @PostMapping("/generate-code")
    public ResponseEntity<String> generateCode(
            @RequestParam String userId,
            @RequestParam AttendanceCodeType type) {  // Đổi String thành AttendanceCodeType
        try {
            String qrContent = attendanceService.generateAttendanceCode(userId, type);
            return ResponseEntity.ok(qrContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API check-in với mã và GPS
    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(
            @RequestParam String userId,
            @RequestParam String code,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String address) {
        try {
            Attendance attendance = attendanceService.checkIn(userId, code, latitude, longitude, address);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            // Trả về lỗi chi tiết thay vì null
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API check-out với mã và GPS
    @PostMapping("/check-out")
    public ResponseEntity<Attendance> checkOut(
            @RequestParam String userId,
            @RequestParam String code,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String address) {
        try {
            Attendance attendance = attendanceService.checkOut(userId, code, latitude, longitude, address);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Xem lịch sử chấm công cá nhân
    @GetMapping("/history")
    public ResponseEntity<List<AttendanceDTO>> searchHistory(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        AttendanceSearchRequest request = new AttendanceSearchRequest();
        request.setUserId(userId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setPage(page);
        request.setSize(size);
        return ResponseEntity.ok(attendanceService.searchAttendanceHistory(request));
    }

    // Xem thống kê cá nhân
    @GetMapping("/summary")
    public ResponseEntity<UserAttendanceSummaryDTO> getUserSummary(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(attendanceService.getUserAttendanceSummary(userId, startDate, endDate));
    }

    // Yêu cầu chỉnh sửa chấm công
    @PostMapping("/request-modification")
    public ResponseEntity<AttendanceModificationRequest> requestModification(
            @RequestBody AttendanceUpdateRequest request,  // Thêm @RequestBody
            @RequestParam String userId) {
        try {
            request.setUserId(userId);
            AttendanceModificationRequest modRequest = attendanceService.createModificationRequest(request);
            return ResponseEntity.ok(modRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}