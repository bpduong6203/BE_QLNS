package DA.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatsDTO {
    private Long totalAttendance;
    private Long onTimeCount;
    private Long lateCount;
    private Long absentCount;
    private Long onLeaveCount;
    private Long overtimeCount;
    private Double totalWorkingHours;
    private Double avgWorkingHours;
    private Double latePercentage;
    private String departmentName;
    private Long departmentId;

    // Thêm thông tin về nghỉ phép
    private Long leaveCount;
    private Double leavePercentage;

    // Tính phần trăm tự động
    public Double getOnTimePercentage() {
        return totalAttendance > 0 ? (onTimeCount * 100.0) / totalAttendance : 0;
    }

    // Thêm thời gian thống kê
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}