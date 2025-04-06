package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    @Column(nullable = false)
    private String status; // PRESENT, LATE, ABSENT, LEAVE

    private String notes;

    // Vị trí GPS khi check in
    private Double checkInLatitude;
    private Double checkInLongitude;
    private String checkInAddress;
    private String checkInCode;

    // Vị trí GPS khi check out
    private Double checkOutLatitude;
    private Double checkOutLongitude;
    private String checkOutAddress;
    private String checkOutCode;



    private Boolean isOvertime = false;
    @Column(scale = 2)
    private Double workingHours;

    // Thông tin chỉnh sửa
    private String lastModifiedBy;
    private LocalDateTime lastModifiedTime;
    private String modificationReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
