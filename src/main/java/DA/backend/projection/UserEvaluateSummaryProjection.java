// UserEvaluateSummaryProjection.java
package DA.backend.projection;

public interface UserEvaluateSummaryProjection {
    String getUserId();
    String getEvaluateName();  // Đổi từ EvaluateId sang EvaluateName
    Integer getTotalScoreUser();
    Integer getTotalScoreAdmin();
    Integer getTotalScoreManager();
}
