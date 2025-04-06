package DA.backend.dto;

import DA.backend.enums.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttendanceUpdateRequest {
    private String userId;
    @NotNull(message = "ID chấm công không được trống")
    private Long attendanceId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private AttendanceStatus status;
    @NotBlank(message = "Lý do chỉnh sửa không được trống")
    private String reason;
    @NotNull(message = "Người chỉnh sửa không được trống")
    private String modifiedBy;

    // Vị trí check-in mới (nếu cần cập nhật)
    private Double checkInLatitude;
    private Double checkInLongitude;
    private String checkInAddress;

    // Vị trí check-out mới (nếu cần cập nhật)
    private Double checkOutLatitude;
    private Double checkOutLongitude;
    private String checkOutAddress;
}