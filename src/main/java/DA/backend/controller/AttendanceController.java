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
            @RequestParam AttendanceCodeType type) { 
        try {
            String qrContent = attendanceService.generateAttendanceCode(userId, type);
            return ResponseEntity.ok(qrContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
}