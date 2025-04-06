package DA.backend.repository;

import DA.backend.entity.UserEvaluate;
import DA.backend.projection.UserEvaluateSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEvaluateRepository extends JpaRepository<UserEvaluate, Long> {
    @Query("SELECT ue FROM UserEvaluate ue WHERE ue.question.id = :questionId")
    List<UserEvaluate> findByQuestionId(@Param("questionId") Long questionId);

    Optional<UserEvaluate> findByUserIdAndEvaluateIdAndQuestionId(String userId, Long evaluateId, Long questionId);

    List<UserEvaluate> findByUserIdAndEvaluateId(String userId, Long evaluateId);
    // Truy vấn mới: Lấy tất cả User được đánh giá trong một kỳ đánh giá cụ thể
    @Query("SELECT ue.user.id AS userId, ue.evaluate.name AS evaluateName, " +
            "SUM(COALESCE(ue.score, 0)) AS totalScoreUser, " +
            "SUM(COALESCE(ue.scoreAdmin, 0)) AS totalScoreAdmin, " +
            "SUM(COALESCE(ue.scoreManager, 0)) AS totalScoreManager " +
            "FROM UserEvaluate ue " +
            "WHERE ue.evaluate.id = :evaluateId " +
            "GROUP BY ue.user.id, ue.evaluate.name")
    List<UserEvaluateSummaryProjection> findAllUserEvaluationsByEvaluateId(@Param("evaluateId") Long evaluateId);
    // 1. Tổng điểm đánh giá của từng User theo từng kỳ đánh giá
    @Query("SELECT ue.user.id AS userId, ue.evaluate.name AS evaluateName, " +
            "SUM(COALESCE(ue.score, 0)) AS totalScoreUser, " +
            "SUM(COALESCE(ue.scoreAdmin, 0)) AS totalScoreAdmin, " +
            "SUM(COALESCE(ue.scoreManager, 0)) AS totalScoreManager " +
            "FROM UserEvaluate ue " +
            "WHERE ue.evaluate.id = :evaluateId " +
            "GROUP BY ue.user.id, ue.evaluate.name")
    List<UserEvaluateSummaryProjection> findUserScoresByEvaluation(@Param("evaluateId") Long evaluateId);

    // 2. Danh sách User đã được đánh giá trong kỳ đánh giá và có trong cùng phòng ban của người dùng đăng nhập
    @Query("SELECT ue.user.id AS userId, ue.evaluate.name AS evaluateName, " +
            "SUM(COALESCE(ue.score, 0)) AS totalScoreUser, " +
            "SUM(COALESCE(ue.scoreAdmin, 0)) AS totalScoreAdmin, " +
            "SUM(COALESCE(ue.scoreManager, 0)) AS totalScoreManager " +
            "FROM UserEvaluate ue " +
            "JOIN ue.user u " +
            "JOIN u.department d " +
            "WHERE ue.evaluate.id = :evaluateId " +
            "AND d.id IN (SELECT dep.id FROM User loginUser JOIN loginUser.department dep WHERE loginUser.id = :userId) " +
            "GROUP BY ue.user.id, ue.evaluate.name")
    List<UserEvaluateSummaryProjection> findEvaluationsByEvaluateAndDepartment(@Param("evaluateId") Long evaluateId, @Param("userId") String userId);

    // 3. Danh sách tất cả các kỳ đánh giá mà User đăng nhập đã tham gia
    @Query("SELECT ue.evaluate.name AS evaluateName, " +
            "SUM(COALESCE(ue.score, 0)) AS totalScoreUser, " +
            "SUM(COALESCE(ue.scoreAdmin, 0)) AS totalScoreAdmin, " +
            "SUM(COALESCE(ue.scoreManager, 0)) AS totalScoreManager " +
            "FROM UserEvaluate ue " +
            "WHERE ue.user.id = :userId " +
            "GROUP BY ue.evaluate.id, ue.evaluate.name")
    List<UserEvaluateSummaryProjection> findAllEvaluationsForUser(@Param("userId") String userId);
    // 4. Tổng điểm đánh giá cho một kỳ đánh giá cụ thể
    @Query("SELECT ue.evaluate.name AS evaluateName, " +
            "SUM(COALESCE(ue.score, 0)) AS totalScoreUser, " +
            "SUM(COALESCE(ue.scoreAdmin, 0)) AS totalScoreAdmin, " +
            "SUM(COALESCE(ue.scoreManager, 0)) AS totalScoreManager " +
            "FROM UserEvaluate ue " +
            "WHERE ue.evaluate.id = :evaluateId " +
            "GROUP BY ue.evaluate.id, ue.evaluate.name")
    List<UserEvaluateSummaryProjection> findTotalScoresByEvaluation(@Param("evaluateId") Long evaluateId);


}
