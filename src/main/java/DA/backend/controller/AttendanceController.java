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
}