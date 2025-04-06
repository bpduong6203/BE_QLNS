package DA.backend.service;

import DA.backend.entity.*;
import DA.backend.projection.UserEvaluateSummaryProjection;
import DA.backend.repository.EvaluateRepository;
import DA.backend.repository.UserEvaluateRepository;
import DA.backend.repository.UserRepository;
import org.docx4j.wml.U;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserEvaluateService {
    @Autowired
    private UserEvaluateRepository userEvaluateRepository;
    @Autowired
    private EvaluateRepository evaluateRepository;
    @Autowired
    private UserRepository userRepository;
    public List<UserEvaluateSummaryProjection> getAllUserEvaluationsByEvaluateId(Long evaluateId) {
        return userEvaluateRepository.findAllUserEvaluationsByEvaluateId(evaluateId);
    }
    public void saveUserEvaluations(List<UserEvaluate> userEvaluates) {
        for (UserEvaluate userEvaluate : userEvaluates) {
            if (userEvaluate.getUser() == null || userEvaluate.getUser().getId() == null) {
                throw new IllegalArgumentException("User or User ID is missing.");
            }
            if (userEvaluate.getQuestion() == null || userEvaluate.getQuestion().getId() == null) {
                throw new IllegalArgumentException("Question or Question ID is missing.");
            }
            if (userEvaluate.getEvaluate() == null || userEvaluate.getEvaluate().getId() == null) {
                throw new IllegalArgumentException("Evaluate or Evaluate ID is missing.");
            }

            Optional<UserEvaluate> optionalUserEvaluate = userEvaluateRepository.findByUserIdAndEvaluateIdAndQuestionId(
                    userEvaluate.getUser().getId(),
                    userEvaluate.getEvaluate().getId(),
                    userEvaluate.getQuestion().getId()
            );

            if (optionalUserEvaluate.isEmpty()) {
                userEvaluateRepository.save(userEvaluate);
            } else {
                UserEvaluate existingUserEvaluate = optionalUserEvaluate.get();
                existingUserEvaluate.setScore(userEvaluate.getScore());
                existingUserEvaluate.setScoreAdmin(userEvaluate.getScoreAdmin());
                existingUserEvaluate.setScoreManager(userEvaluate.getScoreManager());
                userEvaluateRepository.save(existingUserEvaluate);
            }
        }
    }

    public Set<UserEvaluate> lisUserEvaluates(String userId){
        return new HashSet<>(userEvaluateRepository.findAll());
    }

    public int calculateTotalScore(String userId, Long evaluateId) {
        List<UserEvaluate> evaluations = userEvaluateRepository.findByUserIdAndEvaluateId(userId, evaluateId);
        if (evaluations == null || evaluations.isEmpty()) {
            // Trả về 0 nếu không tìm thấy đánh giá nào, hoặc có thể ném ngoại lệ nếu cần
            return 0;
        }
        return evaluations.stream()
                .mapToInt(UserEvaluate::getScore)
                .sum();
    }

    public int calculateTotalScoreAdmin(String userId, Long evaluateId) {
        List<UserEvaluate> evaluations = userEvaluateRepository.findByUserIdAndEvaluateId(userId, evaluateId);
        if (evaluations == null || evaluations.isEmpty()) {
            // Trả về 0 nếu không tìm thấy đánh giá nào
            return 0;
        }

        return evaluations.stream()
                .mapToInt(evaluation -> {
                    Integer scoreAdmin = evaluation.getScoreAdmin();
                    return (scoreAdmin != null) ? scoreAdmin : 0;  // Nếu điểm admin là null, thay thế bằng 0
                })
                .sum();
    }

    public int calculateTotalScoreManager(String userId, Long evaluateId) {
        List<UserEvaluate> evaluations = userEvaluateRepository.findByUserIdAndEvaluateId(userId, evaluateId);
        if (evaluations == null || evaluations.isEmpty()) {
            // Trả về 0 nếu không tìm thấy đánh giá nào
            return 0;
        }

        return evaluations.stream()
                .mapToInt(evaluation -> {
                    Integer scoreManager = evaluation.getScoreManager();
                    return (scoreManager != null) ? scoreManager : 0;  // Nếu điểm manager là null, thay thế bằng 0
                })
                .sum();
    }



    public List<UserEvaluateSummaryProjection> getUserEvaluateSummaries(Long evaluateId) {
        return userEvaluateRepository.findUserScoresByEvaluation(evaluateId);
    }

    public List<UserEvaluateSummaryProjection> getEvaluationsByEvaluateAndDepartment(Long evaluateId, String userId) {
        return userEvaluateRepository.findEvaluationsByEvaluateAndDepartment(evaluateId, userId);
    }

    public List<UserEvaluateSummaryProjection> getEvaluationsByUserId(String userId) {
        return userEvaluateRepository.findAllEvaluationsForUser(userId);
    }
    public Set<UserEvaluate> lisUserEvaluates(Long evaluateId){
        return  new HashSet<>(userEvaluateRepository.findAll());
    }
    // New method to update the scoreAdmin field for existing evaluations
    public void updateAdminScores(List<UserEvaluate> userEvaluates) {
        for (UserEvaluate userEvaluate : userEvaluates) {
            if (userEvaluate.getUser() == null || userEvaluate.getUser().getId() == null) {
                throw new IllegalArgumentException("User or User ID is missing.");
            }
            if (userEvaluate.getQuestion() == null || userEvaluate.getQuestion().getId() == null) {
                throw new IllegalArgumentException("Question or Question ID is missing.");
            }
            if (userEvaluate.getEvaluate() == null || userEvaluate.getEvaluate().getId() == null) {
                throw new IllegalArgumentException("Evaluate or Evaluate ID is missing.");
            }

            Optional<UserEvaluate> optionalUserEvaluate = userEvaluateRepository.findByUserIdAndEvaluateIdAndQuestionId(
                    userEvaluate.getUser().getId(),
                    userEvaluate.getEvaluate().getId(),
                    userEvaluate.getQuestion().getId()
            );

            if (optionalUserEvaluate.isPresent()) {
                // If the evaluation exists, update the scoreAdmin field
                UserEvaluate existingUserEvaluate = optionalUserEvaluate.get();

                existingUserEvaluate.setScoreAdmin(userEvaluate.getScoreAdmin());  // Only updating the scoreAdmin
                userEvaluateRepository.save(existingUserEvaluate);
            } else {
                throw new IllegalArgumentException("User evaluation not found.");
            }
        }
    }
    public void updateManagerScores(List<UserEvaluate> userEvaluates) {
        for (UserEvaluate userEvaluate : userEvaluates) {
            if (userEvaluate.getUser() == null || userEvaluate.getUser().getId() == null) {
                throw new IllegalArgumentException("User or User ID is missing.");
            }
            if (userEvaluate.getQuestion() == null || userEvaluate.getQuestion().getId() == null) {
                throw new IllegalArgumentException("Question or Question ID is missing.");
            }
            if (userEvaluate.getEvaluate() == null || userEvaluate.getEvaluate().getId() == null) {
                throw new IllegalArgumentException("Evaluate or Evaluate ID is missing.");
            }

            Optional<UserEvaluate> optionalUserEvaluate = userEvaluateRepository.findByUserIdAndEvaluateIdAndQuestionId(
                    userEvaluate.getUser().getId(),
                    userEvaluate.getEvaluate().getId(),
                    userEvaluate.getQuestion().getId()
            );

            if (optionalUserEvaluate.isPresent()) {
                // If the evaluation exists, update the scoreAdmin field
                UserEvaluate existingUserEvaluate = optionalUserEvaluate.get();

                existingUserEvaluate.setScoreManager(userEvaluate.getScoreManager());  // Only updating the scoreAdmin
                userEvaluateRepository.save(existingUserEvaluate);
            } else {
                throw new IllegalArgumentException("User evaluation not found.");
            }
        }
    }
}
