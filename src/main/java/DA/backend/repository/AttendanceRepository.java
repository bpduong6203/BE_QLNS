package DA.backend.repository;

import DA.backend.dto.DailyAttendanceStatsDTO;
import DA.backend.dto.OvertimeStatsDTO;
import DA.backend.dto.UserAttendanceStatsDTO;
import DA.backend.entity.Attendance;
import DA.backend.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Tìm bản ghi chấm công mới nhất của user trong ngày
    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId " +
            "AND DATE(a.checkInTime) = CURRENT_DATE " +
            "ORDER BY a.checkInTime DESC")
    Optional<Attendance> findLatestAttendanceByUserId(@Param("userId") String userId);

    // Tìm kiếm theo nhiều tiêu chí
    @Query("SELECT a FROM Attendance a " +
            "JOIN a.user u " +
            "JOIN u.department d " +
            "WHERE (:userId IS NULL OR a.user.id = :userId) " +
            "AND (:departmentId IS NULL OR d.id = :departmentId) " +
            "AND (:startDate IS NULL OR a.checkInTime >= :startDate) " +
            "AND (:endDate IS NULL OR a.checkInTime <= :endDate) " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<Attendance> findBySearchCriteria(
            @Param("userId") String userId,
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") AttendanceStatus status,
            Pageable pageable);


    // Lấy lịch sử chấm công của user trong khoảng thời gian
    List<Attendance> findByUserIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(
            String userId, LocalDateTime startDate, LocalDateTime endDate);

    // Thống kê số ngày đi muộn trong tháng
    @Query("SELECT COUNT(a) FROM Attendance a " +
            "WHERE a.user.id = :userId " +
            "AND MONTH(a.checkInTime) = MONTH(CURRENT_DATE) " +
            "AND YEAR(a.checkInTime) = YEAR(CURRENT_DATE) " +
            "AND a.status = 'LATE'")
    Long countLateAttendancesInCurrentMonth(@Param("userId") String userId);

    @Query("SELECT a FROM Attendance a " +
            "WHERE a.user.id = :userId " +
            "AND a.checkInTime BETWEEN :startDate AND :endDate")
    Page<Attendance> findByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT a FROM Attendance a " +
            "WHERE EXISTS (SELECT 1 FROM a.user.department d WHERE d.id = :departmentId) " +
            "AND a.checkInTime BETWEEN :startDate AND :endDate")
    List<Attendance> findByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT NEW DA.backend.dto.UserAttendanceStatsDTO(" +
            "u.id, u.name, d.name, u.position.name, " +
            "COUNT(a), " +
            "COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END), " +
            "COUNT(CASE WHEN a.status = 'LATE' THEN 1 END), " +
            "COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END), " +
            "COUNT(CASE WHEN a.isOvertime = true THEN 1 END), " +
            "SUM(a.workingHours)) " +
            "FROM Attendance a JOIN a.user u JOIN u.department d " +
            "WHERE d.id = :departmentId " +
            "AND a.checkInTime BETWEEN :startDate AND :endDate " +
            "GROUP BY u.id, u.name, d.name, u.position.name")
    List<UserAttendanceStatsDTO> findUserStatsInDepartment(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT NEW DA.backend.dto.OvertimeStatsDTO(" +
            "u.id, " +
            "u.name, " +
            "d.name, " +
            "SUM(a.workingHours)) " +
            "FROM Attendance a " +
            "JOIN a.user u " +
            "JOIN u.department d " +
            "WHERE d.id = :departmentId " +
            "AND a.checkInTime BETWEEN :startDate AND :endDate " +
            "AND a.isOvertime = true " +
            "GROUP BY u.id, u.name, d.name")
    List<OvertimeStatsDTO> findOvertimeStats(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT COUNT(DISTINCT DATE(a.check_in_time)) " +
            "FROM attendance a " +
            "WHERE a.user_id = :userId " +
            "AND a.is_overtime = true " +
            "AND a.check_in_time BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    Integer countOvertimeDays(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Attendance a " +
            "WHERE (:userId IS NULL OR a.user.id = :userId) " +
            "AND (:departmentId IS NULL OR :departmentId IN (SELECT d.id FROM a.user.department d)) " +
            "AND (:startDate IS NULL OR a.checkInTime >= :startDate) " +
            "AND (:endDate IS NULL OR a.checkInTime <= :endDate) " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<Attendance> searchAttendance(
            @Param("userId") String userId,
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") String status,
            Pageable pageable
    );

    // Thống kê theo khoảng thời gian
    @Query("SELECT NEW DA.backend.dto.DailyAttendanceStatsDTO(" +
            "DATE(a.checkInTime), COUNT(DISTINCT a.user.id), " +
            "COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END), " +
            "COUNT(CASE WHEN a.status = 'LATE' THEN 1 END), " +
            "COUNT(CASE WHEN a.status = 'ABSENT' THEN 1 END), " +
            "COUNT(CASE WHEN a.isOvertime = true THEN 1 END), " +
            "AVG(HOUR(a.checkInTime) * 60 + MINUTE(a.checkInTime)), " +
            "AVG(HOUR(a.checkOutTime) * 60 + MINUTE(a.checkOutTime))) " +
            "FROM Attendance a " +
            "WHERE a.checkInTime BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(a.checkInTime)")
    List<DailyAttendanceStatsDTO> getDailyStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    Optional<Attendance> findFirstByUserIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(
            String userId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}