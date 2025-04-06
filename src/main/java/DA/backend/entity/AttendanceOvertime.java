package DA.backend.entity;

import DA.backend.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_overtime")
@Data
public class AttendanceOvertime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Column(nullable = false)
    private Double standardHours;

    @Column(nullable = false)
    private Double extraHours;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    private String approvedBy;
    private LocalDateTime approvedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Relationship với User nếu cần
    @ManyToOne
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;
}