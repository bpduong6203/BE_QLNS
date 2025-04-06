package DA.backend.repository;

import DA.backend.entity.UserDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {

    void deleteByUserId(String userId);

    @Query("SELECT ud FROM UserDepartment ud WHERE ud.userId = :userId")
    UserDepartment findByUserId(@Param("userId") String userId);

    // Thêm method để kiểm tra department
    @Query("SELECT CASE WHEN COUNT(ud) > 0 THEN true ELSE false END FROM UserDepartment ud WHERE ud.userId = :userId")
    boolean existsByUserId(@Param("userId") String userId);
}