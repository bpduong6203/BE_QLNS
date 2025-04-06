package DA.backend.repository;

import DA.backend.entity.QuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionSetRepository extends JpaRepository<QuestionSet,Long> {
}
