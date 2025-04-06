package DA.backend.repository;

import DA.backend.entity.AttendanceAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceAuditLogRepository extends JpaRepository<AttendanceAuditLog, Long> {

    // Tìm lịch sử chỉnh sửa của một bản ghi chấm công
    List<AttendanceAuditLog> findByAttendanceIdOrderByModificationTimeDesc(Long attendanceId);

    // Tìm lịch sử chỉnh sửa theo người thực hiện
    List<AttendanceAuditLog> findByModifiedByOrderByModificationTimeDesc(String modifiedBy);

    // Tìm lịch sử chỉnh sửa trong khoảng thời gian
    @Query("SELECT a FROM AttendanceAuditLog a WHERE a.modificationTime BETWEEN :startDate AND :endDate")
    List<AttendanceAuditLog> findByModificationTimeBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}