package DA.backend.repository;

import DA.backend.entity.AttendanceModificationRequest;
import DA.backend.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceModificationRequestRepository extends JpaRepository<AttendanceModificationRequest, Long> {

    // Tìm các yêu cầu sửa đổi của một người dùng
    @Query("SELECT amr FROM AttendanceModificationRequest amr " +
            "WHERE amr.requestedBy.id = :userId " +
            "ORDER BY amr.requestTime DESC")
    List<AttendanceModificationRequest> findByRequestedByOrderByRequestTimeDesc(@Param("userId") String userId);


    // Tìm các yêu cầu chưa được phê duyệt
    List<AttendanceModificationRequest> findByApprovedFalseOrderByRequestTimeDesc();

    // Tìm các yêu cầu theo trạng thái phê duyệt
    List<AttendanceModificationRequest> findByApproved(boolean approved);

    // Tìm các yêu cầu theo người phê duyệt
    List<AttendanceModificationRequest> findByApprovedByOrderByApprovalTimeDesc(String approverId);

    // Đếm số yêu cầu đang chờ xử lý của một phòng ban
    @Query("SELECT COUNT(m) FROM AttendanceModificationRequest m " +
            "JOIN m.requestedBy u " +
            "JOIN u.department d " +
            "WHERE d.id = :departmentId AND m.approved = false")
    Long countPendingRequestsByDepartment(@Param("departmentId") Long departmentId);

    // Tìm các yêu cầu đang chờ duyệt của phòng ban
    @Query("SELECT amr FROM AttendanceModificationRequest amr " +
            "JOIN amr.requestedBy u " +
            "JOIN u.department d " +
            "WHERE d.id = :departmentId AND amr.status = 'PENDING'")
    List<AttendanceModificationRequest> findByDepartmentIdAndApprovedFalse(@Param("departmentId") Long departmentId);

    @Query("SELECT amr FROM AttendanceModificationRequest amr " +
            "WHERE amr.status = :status " +
            "ORDER BY amr.requestTime DESC")
    List<AttendanceModificationRequest> findByStatusOrderByRequestTimeDesc(@Param("status") ApprovalStatus status);

    @Query("SELECT r FROM AttendanceModificationRequest r " +
            "JOIN r.attendance a " +
            "JOIN a.user u " +
            "JOIN u.department d " +
            "WHERE d.id = :departmentId AND r.status = :status " +
            "ORDER BY r.requestTime DESC")
    List<AttendanceModificationRequest> findByDepartmentIdAndStatus(
            @Param("departmentId") Long departmentId,
            @Param("status") ApprovalStatus status
    );
}