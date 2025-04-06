package DA.backend.repository;

import DA.backend.entity.AttendanceCode;
import DA.backend.enums.AttendanceCodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceCodeRepository extends JpaRepository<AttendanceCode, Long> {

    // Tìm mã chưa sử dụng theo user, code và type
    Optional<AttendanceCode> findByUserIdAndCodeAndTypeAndIsUsedFalse(
            String userId,
            String code,
            AttendanceCodeType type
    );

    // Tìm mã mới nhất của user theo type
    @Query("SELECT ac FROM AttendanceCode ac WHERE ac.user.id = :userId " +
            "AND ac.type = :type AND ac.isUsed = false " +
            "AND ac.expirationTime > CURRENT_TIMESTAMP " +
            "ORDER BY ac.createdAt DESC")
    Optional<AttendanceCode> findLatestValidCode(
            @Param("userId") String userId,
            @Param("type") AttendanceCodeType type
    );

    // Xóa các mã đã hết hạn
    @Modifying
    @Transactional
    void deleteByExpirationTimeBefore(LocalDateTime dateTime);

    // Tìm tất cả mã hết hạn
    List<AttendanceCode> findByExpirationTimeBefore(LocalDateTime dateTime);

    // Tìm tất cả mã của user trong ngày
    @Query("SELECT ac FROM AttendanceCode ac WHERE ac.user.id = :userId " +
            "AND DATE(ac.createdAt) = CURRENT_DATE")
    List<AttendanceCode> findTodaysCodes(@Param("userId") String userId);

    // Đếm số lần tạo mã trong ngày của user
    @Query("SELECT COUNT(ac) FROM AttendanceCode ac WHERE ac.user.id = :userId " +
            "AND DATE(ac.createdAt) = CURRENT_DATE")
    Long countTodaysCodesByUser(@Param("userId") String userId);

    // Kiểm tra xem mã có hợp lệ không
    @Query("SELECT CASE WHEN COUNT(ac) > 0 THEN true ELSE false END FROM AttendanceCode ac " +
            "WHERE ac.code = :code AND ac.user.id = :userId AND ac.type = :type " +
            "AND ac.isUsed = false AND ac.expirationTime > CURRENT_TIMESTAMP")
    boolean isCodeValid(
            @Param("code") String code,
            @Param("userId") String userId,
            @Param("type") String type
    );

    // Vô hiệu hóa tất cả mã cũ của user theo type
    @Modifying
    @Transactional
    @Query("UPDATE AttendanceCode ac SET ac.isUsed = true " +
            "WHERE ac.user.id = :userId AND ac.type = :type AND ac.isUsed = false")
    void invalidateAllUserCodes(
            @Param("userId") String userId,
            @Param("type") AttendanceCodeType type
    );
}