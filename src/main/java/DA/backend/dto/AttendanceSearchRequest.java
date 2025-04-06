package DA.backend.dto;

import DA.backend.enums.AttendanceStatus;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttendanceSearchRequest {
    private String userId;
    private Long departmentId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int page;
    private int size;

    // Thêm tiêu chí tìm kiếm
    private Boolean isOvertime;
    private String sortBy;
    private String sortDirection;

    // Sử dụng enum
    private AttendanceStatus status;

    // Kiểm tra range thời gian
    @AssertTrue(message = "Thời gian kết thúc phải sau thời gian bắt đầu")
    private boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return true;
        return !endDate.isBefore(startDate);
    }
}
