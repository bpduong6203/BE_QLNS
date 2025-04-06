package DA.backend.repository;

import DA.backend.entity.Department;
import DA.backend.entity.User;
import DA.backend.entity.User_Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<User_Activity, Long> {
    List<User_Activity> findByActivityId(Long activityId);
    List<User_Activity> findByActivityIdAndStatusFalse(Long activityId);
    // Adjusted method to find users by activity ID and department ID
    List<User_Activity> findByActivityIdAndUser_Department_Id(Long activityId, Long departmentId);
    Optional<User_Activity> findByUserIdAndActivityId(String userId, Long activityId);
     List<User_Activity> findByActivityIdAndStatusTrue(Long activityId);

    @Query("SELECT ua.user FROM User_Activity ua WHERE ua.activity.id = :activityId")
    List<User> findUsersByActivityId(@Param("activityId") Long activityId);



}

