package DA.backend.entity;

import DA.backend.enums.ApprovalStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_modification_request")
@Data
public class AttendanceModificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attendance_id")
    private Attendance attendance;

    @ManyToOne
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    private LocalDateTime requestTime;
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime requestedCheckInTime;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime requestedCheckOutTime;
    private String reason;
    private boolean approved;
    private String approvedBy;
    private LocalDateTime approvalTime;
    private String approvalComment;

    // Vị trí check-in mới
    private Double requestedCheckInLatitude;
    private Double requestedCheckInLongitude;
    private String requestedCheckInAddress;

    // Vị trí check-out mới
    private Double requestedCheckOutLatitude;
    private Double requestedCheckOutLongitude;
    private String requestedCheckOutAddress;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }
}
