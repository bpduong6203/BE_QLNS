package DA.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RemainingLeaveDTO {
    private int annualLeaveRemaining;
    private int sickLeaveRemaining;
    private int unpaidLeaveRemaining;
    private int totalLeaveTaken;
    private int pendingRequests;
}