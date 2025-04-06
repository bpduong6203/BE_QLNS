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
public class UserAttendanceSummaryDTO {
    private String userId;
    private String userName;
    private String departmentName;
    private int totalDays;
    private int presentDays;
    private int lateDays;
    private int absentDays;
    private double totalWorkingHours;
    private double averageWorkingHours;
    private int overtimeDays;
    private int leaveDays;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    // Tính các tỷ lệ
    public double getAttendanceRate() {
        return totalDays > 0 ? (presentDays * 100.0) / totalDays : 0;
    }
}