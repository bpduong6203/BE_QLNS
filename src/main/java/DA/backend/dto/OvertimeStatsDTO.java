package DA.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeStatsDTO {
    private String userId;
    private String userName;
    private String departmentName;
    private Double overtimeHours;
    private Integer overtimeDays;

    // Constructor for JPA projection
    public OvertimeStatsDTO(String userId, String userName, String departmentName, Double overtimeHours) {
        this.userId = userId;
        this.userName = userName;
        this.departmentName = departmentName;
        this.overtimeHours = overtimeHours != null ? overtimeHours : 0.0;
        this.overtimeDays = 0;
    }
}