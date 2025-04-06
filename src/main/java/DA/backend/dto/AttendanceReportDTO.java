package DA.backend.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AttendanceReportDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String departmentName;
    private Long departmentId;
    private List<UserAttendanceStatsDTO> userStats;
    private DepartmentStatsDTO departmentStats;
    private List<OvertimeStatsDTO> overtimeStats;

    // Thông tin tổng hợp
    private Integer totalEmployees;
    private Double averageAttendanceRate;
    private Double averageLateRate;
    private Double totalOvertimeHours;
    private Double averageWorkingHours;

    // Thêm thông tin về nghỉ phép
    private Double leaveRate;
    private int totalLeaveCount;

    // Thống kê theo ca làm việc
    private int morningShiftCount;
    private int afternoonShiftCount;

    // Thông tin về điểm danh qua GPS
    private int inOfficeCount;
    private int remoteWorkCount;
}