package DA.backend.repository;

import DA.backend.entity.UserLeaveInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLeaveInfoRepository extends JpaRepository<UserLeaveInfo, Long> {
    Optional<UserLeaveInfo> findByUserId(String userId);
}