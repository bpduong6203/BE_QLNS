package DA.backend.controller;

import DA.backend.dto.DepartmentLeaveStatsDTO;
import DA.backend.entity.Leave;
import DA.backend.enums.LeaveStatus;
import DA.backend.enums.LeaveType;
import DA.backend.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave/manager")
@CrossOrigin
public class LeaveManagerController {

    @Autowired
    private LeaveService leaveService;

    // Lấy danh sách đơn xin nghỉ chờ duyệt
    @GetMapping("/pending-requests")
    public ResponseEntity<List<Leave>> getPendingRequests(
            @RequestParam Long departmentId
    ) {
        List<Leave> pendingLeaves = leaveService.getPendingLeavesByDepartment(departmentId);
        return ResponseEntity.ok(pendingLeaves);
    }

    // Phê duyệt đơn xin nghỉ
    @PostMapping("/{leaveId}/approve")
    public ResponseEntity<Leave> approveLeaveRequest(
            @PathVariable Long leaveId,
            @RequestParam String approverId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment
    ) {
        try {
            Leave leave = leaveService.approveLeave(leaveId, approverId, approved, comment);
            return ResponseEntity.ok(leave);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Thống kê nghỉ phép theo phòng ban
    @GetMapping("/department-stats")
    public ResponseEntity<DepartmentLeaveStatsDTO> getDepartmentLeaveStats(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        DepartmentLeaveStatsDTO stats = leaveService.getDepartmentLeaveStats(
                departmentId, startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    // Xuất báo cáo nghỉ phép
    @GetMapping("/export")
    public ResponseEntity<String> exportLeaveReport(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        byte[] report = leaveService.exportLeaveReport(departmentId, startDate, endDate);
        String base64 = Base64.getEncoder().encodeToString(report);
        return ResponseEntity.ok(base64);
    }

    // Cập nhật số ngày nghỉ phép cho nhân viên
    @PutMapping("/update-leave-days")
    public ResponseEntity<?> updateLeaveDays(
            @RequestParam String userId,
            @RequestParam int annualLeaveDays
    ) {
        try {
            leaveService.updateAnnualLeaveDays(userId, annualLeaveDays);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{leaveId}/evidence")
    public ResponseEntity<String> getLeaveEvidence(@PathVariable Long leaveId) {
        try {
            String evidence = leaveService.getLeaveEvidence(leaveId);
            if (evidence != null) {
                return ResponseEntity.ok()
                        .header("Content-Type", "image/png;base64")
                        .body(evidence);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Lấy danh sách đơn xin nghỉ theo khoảng thời gian
    @GetMapping("/leaves")
    public ResponseEntity<List<Leave>> getLeavesByDateRange(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) LeaveStatus status
    ) {
        try {
            List<Leave> leaves = leaveService.getLeavesByDateRange(departmentId, startDate, endDate, status);
            return ResponseEntity.ok(leaves);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Lấy chi tiết đơn xin nghỉ
    @GetMapping("/{leaveId}/details")
    public ResponseEntity<Leave> getLeaveDetails(@PathVariable Long leaveId) {
        try {
            Leave leave = leaveService.getLeaveById(leaveId);
            return ResponseEntity.ok(leave);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Thống kê nghỉ phép theo loại
    @GetMapping("/stats/by-type")
    public ResponseEntity<?> getLeaveStatsByType(
            @RequestParam Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        try {
            Map<LeaveType, Integer> stats = leaveService.getLeaveStatsByType(departmentId, startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy danh sách nhân viên đang nghỉ
    @GetMapping("/current-leaves")
    public ResponseEntity<List<Leave>> getCurrentLeaves(@RequestParam Long departmentId) {
        try {
            List<Leave> currentLeaves = leaveService.getCurrentLeaves(departmentId);
            return ResponseEntity.ok(currentLeaves);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Hủy duyệt đơn xin nghỉ (trong trường hợp duyệt nhầm)
    @PostMapping("/{leaveId}/revoke")
    public ResponseEntity<Leave> revokeApproval(
            @PathVariable Long leaveId,
            @RequestParam String managerId,
            @RequestParam String reason
    ) {
        try {
            Leave leave = leaveService.revokeLeaveApproval(leaveId, managerId, reason);
            return ResponseEntity.ok(leave);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Xuất báo cáo theo từng nhân viên
    @GetMapping("/export/by-employee")
    public ResponseEntity<byte[]> exportLeaveReportByEmployee(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        byte[] report = leaveService.exportLeaveReportByEmployee(userId, startDate, endDate);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=employee_leave_report.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(report);
    }


}