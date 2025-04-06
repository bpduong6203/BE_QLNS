package DA.backend.dto;

import DA.backend.enums.AttendanceStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttendanceDTO {
    private Long id;
    private String userId;
    private String userName;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Double workingHours;
    private Boolean isOvertime;
    private String checkInLocation;
    private String checkOutLocation;

    private Double checkInLatitude;
    private Double checkInLongitude;
    private String checkInAddress;
    private String checkInCode;

    private Double checkOutLatitude;
    private Double checkOutLongitude;
    private String checkOutAddress;
    private String checkOutCode;

    // Thông tin sửa đổi
    private String lastModifiedBy;
    private LocalDateTime lastModifiedTime;
    private String modificationReason;

    // Sử dụng enum
    private AttendanceStatus status;
}