package DA.backend.repository;

import DA.backend.entity.Leave;
import DA.backend.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Leave> findByStatusOrderByCreatedAtDesc(LeaveStatus status);

    @Query("SELECT l FROM Leave l " +
            "JOIN l.user u JOIN u.department d " +
            "WHERE d.id = :departmentId " +
            "AND l.startDate <= :endDate " +
            "AND l.endDate >= :startDate")
    List<Leave> findByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT l FROM Leave l " +
            "JOIN l.user u JOIN u.department d " +
            "WHERE d.id = :departmentId AND l.status = :status " +
            "ORDER BY l.createdAt DESC")
    List<Leave> findByDepartmentAndStatus(
            @Param("departmentId") Long departmentId,
            @Param("status") LeaveStatus status
    );

    @Query("SELECT COUNT(l) > 0 FROM Leave l " +
            "WHERE l.user.id = :userId " +
            "AND l.status = 'APPROVED' " +
            "AND :date BETWEEN l.startDate AND l.endDate")
    boolean isUserOnLeave(
            @Param("userId") String userId,
            @Param("date") LocalDateTime date
    );

    @Query("SELECT l FROM Leave l " +
            "JOIN l.user u JOIN u.department d " +
            "WHERE d.id = :departmentId " +
            "AND l.startDate <= :endDate " +
            "AND l.endDate >= :startDate " +
            "AND l.status = :status")
    List<Leave> findByDepartmentAndDateRangeAndStatus(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") LeaveStatus status
    );

    @Query("SELECT l FROM Leave l " +
            "WHERE l.user.id = :userId " +
            "AND l.startDate <= :endDate " +
            "AND l.endDate >= :startDate")
    List<Leave> findByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}