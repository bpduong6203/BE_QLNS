package DA.backend.repository;

import DA.backend.entity.AttendanceOvertime;
import DA.backend.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceOvertimeRepository extends JpaRepository<AttendanceOvertime, Long> {

    List<AttendanceOvertime> findByAttendanceUserIdAndStatusOrderByCreatedAtDesc(
            String userId,
            ApprovalStatus status
    );

    List<AttendanceOvertime> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            ApprovalStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT ao FROM AttendanceOvertime ao " +
            "JOIN ao.attendance a " +
            "JOIN a.user u " +
            "JOIN u.department d " +
            "WHERE d.id = :departmentId " +
            "AND ao.createdAt BETWEEN :startDate AND :endDate")
    List<AttendanceOvertime> findByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}