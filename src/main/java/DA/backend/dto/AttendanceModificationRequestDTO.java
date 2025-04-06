package DA.backend.dto;

import DA.backend.enums.ModificationRequestStatus;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class AttendanceModificationRequestDTO {
    private Long id;
    private Long attendanceId;
    private String userId;
    private String requestedBy;
    private LocalDateTime requestTime;
    private LocalDateTime requestedCheckInTime;
    private LocalDateTime requestedCheckOutTime;
    private String reason;
    private boolean approved;
    private String approvedBy;
    private LocalDateTime approvalTime;
    private String approvalComment;
    private Double requestedCheckInLatitude;
    private Double requestedCheckInLongitude;
    private String requestedCheckInAddress;
    private Double requestedCheckOutLatitude;
    private Double requestedCheckOutLongitude;
    private String requestedCheckOutAddress;

    private ModificationRequestStatus status = ModificationRequestStatus.PENDING;
}
