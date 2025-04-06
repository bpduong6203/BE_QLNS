package DA.backend.repository;

import DA.backend.entity.Role;
import DA.backend.entity.TimeEvaluateRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimeEvaluateRoleRepository extends JpaRepository<TimeEvaluateRole,Long> {

        Optional<TimeEvaluateRole> findByEvaluateId(Long evaluateId);
    Optional<Role> findByRoleId(Long roleId);
    Optional<TimeEvaluateRole> findByEvaluateIdAndRoleId(Long evaluateId, Long roleId);

}
