package DA.backend.repository;

import DA.backend.entity.WorkingTimeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkingTimeConfigRepository extends JpaRepository<WorkingTimeConfig, Long> {
    @Query("SELECT w FROM WorkingTimeConfig w WHERE w.id = (SELECT MAX(wc.id) FROM WorkingTimeConfig wc)")
    WorkingTimeConfig findActive();
}
