package DA.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_leave_info")
@Data
public class UserLeaveInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int annualLeaveDays;
    private int sickLeaveDays;
    private LocalDateTime updatedAt;
}