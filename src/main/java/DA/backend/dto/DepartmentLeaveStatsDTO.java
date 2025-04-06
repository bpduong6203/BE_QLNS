package DA.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentLeaveStatsDTO {
    private Long departmentId;
    private Long totalEmployees;
    private Long onLeaveCount;
    private Long pendingCount;
    private Double totalLeaveDays;
    private Double averageLeaveDays;
}