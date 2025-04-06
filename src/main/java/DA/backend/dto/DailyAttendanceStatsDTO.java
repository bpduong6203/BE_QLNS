package DA.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class DailyAttendanceStatsDTO {
    private LocalDate date;
    private long totalEmployees;
    private long presentCount;
    private long lateCount;
    private long absentCount;
    private long overtimeCount;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime averageArrivalTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime averageDepartureTime;

    // Thêm thông tin về nghỉ phép
    private long leaveCount;
    private double leavePercentage;

    // Thêm thông tin làm remote
    private long remoteWorkCount;

    // Thông tin về tổng giờ làm
    private double totalWorkingHours;
    private double avgWorkingHours;

    // Tính phần trăm
    public double getPresentPercentage() {
        return totalEmployees > 0 ? (presentCount * 100.0) / totalEmployees : 0;
    }

    public double getLatePercentage() {
        return totalEmployees > 0 ? (lateCount * 100.0) / totalEmployees : 0;
    }

    public double getAbsentPercentage() {
        return totalEmployees > 0 ? (absentCount * 100.0) / totalEmployees : 0;
    }

    public double getOvertimePercentage() {
        return totalEmployees > 0 ? (overtimeCount * 100.0) / totalEmployees : 0;
    }
}