package DA.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class UserAttendanceStatsDTO {
    private String userId;
    private String userName;
    private String departmentName;
    private String position;
    private Long totalDays;
    private Long presentDays;
    private Long lateDays;
    private Long absentDays;
    private Long overtimeDays;
    private Double totalWorkingHours;
    private Double avgWorkingHoursPerDay;
    private Long consecutiveLateDays;
    private LocalTime mostFrequentCheckInTime;
    private LocalTime mostFrequentCheckOutTime;
    private String email;
    private String phoneNumber;
    private Long leaveDays;
    private Double leavePercentage;
    public Double getPresentRate() {
        return totalDays > 0 ? (presentDays * 100.0) / totalDays : 0;
    }
}