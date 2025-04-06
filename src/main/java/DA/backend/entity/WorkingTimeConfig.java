package DA.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Entity
public class WorkingTimeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Giờ làm việc chuẩn
    private LocalTime workStartTime = LocalTime.of(8, 0);  // 8:00 AM
    private LocalTime workEndTime = LocalTime.of(17, 30);  // 5:30 PM

    // Thời gian buffer cho phép check-in sớm/muộn
    private Integer earlyCheckInMinutes = 30;  // Cho phép check in sớm 30p
    private Integer lateCheckInMinutes = 15;   // Cho phép đi muộn 15p

    // Thời gian tối thiểu/tối đa làm việc trong ngày
    private Double minWorkingHours = 8.0;
    private Double maxWorkingHours = 12.0;

    // Cấu hình ngày làm việc trong tuần
    private boolean monday = true;
    private boolean tuesday = true;
    private boolean wednesday = true;
    private boolean thursday = true;
    private boolean friday = true;
    private boolean saturday = false;
    private boolean sunday = false;
}

