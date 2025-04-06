package DA.backend.controller;

import DA.backend.entity.Question;
import DA.backend.entity.User;
import DA.backend.entity.UserEvaluate;
import DA.backend.projection.UserEvaluateSummaryProjection;
import DA.backend.repository.UserEvaluateRepository;
import DA.backend.service.EvaluateService;
import DA.backend.service.UserEvaluateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/userEvaluate")
@CrossOrigin
public class UserEvaluateController {

    @Autowired
    private UserEvaluateService userEvaluateService;
    @Autowired
    EvaluateService evaluateService;
    @Autowired
    UserEvaluateRepository userEvaluateRepository;
    @GetMapping("/allUserEvaluationsByEvaluateId")
    public ResponseEntity<List<UserEvaluateSummaryProjection>> getAllUserEvaluationsByEvaluateId(@RequestParam Long evaluateId) {
        List<UserEvaluateSummaryProjection> summaries = userEvaluateService.getAllUserEvaluationsByEvaluateId(evaluateId);
        return ResponseEntity.ok(summaries);
    }
    @PostMapping("/evaluate")
    public ResponseEntity<Void> evaluateUser(@RequestBody List<UserEvaluate> userEvaluates) {
        userEvaluateService.saveUserEvaluations(userEvaluates);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getUserAnswers")
    public ResponseEntity<List<UserEvaluate>> getUserAnswers(
            @RequestParam String userId,
            @RequestParam Long evaluateId) {
        try {
            List<UserEvaluate> userAnswers = userEvaluateRepository.findByUserIdAndEvaluateId(userId, evaluateId);
            return ResponseEntity.ok(userAnswers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("listUserEvaluateByUser")
    public  ResponseEntity<Set<UserEvaluate>> listUserEvaluateByUser(@RequestParam Long evaluateId){
        Set<UserEvaluate> userEvaluates = userEvaluateService.lisUserEvaluates(evaluateId);
        return ResponseEntity.ok(userEvaluates);
    }
    @GetMapping("/calculateTotalScore")
    public ResponseEntity<Integer> calculateTotalScore(@RequestParam String userId, @RequestParam Long evaluateId) {
        int totalScore = userEvaluateService.calculateTotalScore(userId, evaluateId);
        return ResponseEntity.ok(totalScore);
    }

    @GetMapping("/calculateTotalScoreAdmin")
    public ResponseEntity<Integer> calculateTotalScoreAdmin(@RequestParam String userId, @RequestParam Long evaluateId) {
        Integer totalScoreAdmin = userEvaluateService.calculateTotalScoreAdmin(userId, evaluateId);
        return ResponseEntity.ok(totalScoreAdmin);
    }

    @GetMapping("/calculateTotalScoreManager")
    public ResponseEntity<Integer> calculateTotalScoreManager(@RequestParam String userId, @RequestParam Long evaluateId) {
        int totalScoreManager = userEvaluateService.calculateTotalScoreManager(userId, evaluateId);
        return ResponseEntity.ok(totalScoreManager);
    }

    @GetMapping("/userEvaluations")
    public ResponseEntity<List<UserEvaluateSummaryProjection>> getUserEvaluations(@RequestParam String userId) {
        List<UserEvaluateSummaryProjection> evaluations = userEvaluateService.getEvaluationsByUserId(userId);
        return ResponseEntity.ok(evaluations);
    }

    @GetMapping("/evaluateByDepartment")
    public ResponseEntity<List<UserEvaluateSummaryProjection>> getEvaluateByDepartment(
            @RequestParam Long evaluateId,
            @RequestParam String userId) {
        List<UserEvaluateSummaryProjection> summaries = userEvaluateService.getEvaluationsByEvaluateAndDepartment(evaluateId, userId);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/evaluateSummary")
    public ResponseEntity<List<UserEvaluateSummaryProjection>> getEvaluateSummary(@RequestParam Long evaluateId) {
        List<UserEvaluateSummaryProjection> summaries = userEvaluateService.getUserEvaluateSummaries(evaluateId);
        return ResponseEntity.ok(summaries);
    }
    @GetMapping("/listUserEvaluates")
    public ResponseEntity<Set<UserEvaluate>> listUserEvaluates(@RequestParam String userId) {
        System.out.println("Received userId: " + userId); // Log userId for debugging
        try {
            Set<UserEvaluate> userEvaluates = userEvaluateService.lisUserEvaluates(userId);
            System.out.println("Fetched UserEvaluates: " + userEvaluates); // Log fetched data
            return ResponseEntity.ok(userEvaluates);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Return 500 if there's an error
        }
    }
    @GetMapping("/questions")
    public ResponseEntity<List<Question>> listQuestionsByEvaluate(@RequestParam Long evaluateId) {
        List<Question> questions = evaluateService.listQuestion(evaluateId);
        return ResponseEntity.ok(questions);
    }

    @PutMapping("/updateAdminScores")
    public ResponseEntity<Void> updateAdminScores(@RequestBody List<UserEvaluate> userEvaluates) {
        try {
            userEvaluateService.updateAdminScores(userEvaluates);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // If invalid data is provided
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // If server error
        }
    }
    @PutMapping("/updateManagerScores")
    public ResponseEntity<Void> updateManagerScores(@RequestBody List<UserEvaluate> userEvaluates) {
        try {
            userEvaluateService.updateManagerScores(userEvaluates);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // If invalid data is provided
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // If server error
        }
    }

}
