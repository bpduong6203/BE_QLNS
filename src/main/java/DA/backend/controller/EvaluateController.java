package DA.backend.controller;

import DA.backend.entity.Evaluate;
import DA.backend.entity.Question;
import DA.backend.service.EvaluateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluate")
@CrossOrigin
public class EvaluateController {

    @Autowired
    private EvaluateService evaluateService;

    @PostMapping("/add")
    public ResponseEntity<Boolean> addEvaluate(@RequestBody Evaluate evaluate) {
        return ResponseEntity.ok(evaluateService.add(evaluate));
    }

    @PutMapping("/update")
    public ResponseEntity<Boolean> updateEvaluate(@RequestBody Evaluate evaluate) {
        return ResponseEntity.ok(evaluateService.update(evaluate));
    }

    @PostMapping("/addQuestionSet")
    public ResponseEntity<Boolean> addQuestionSet(@RequestParam(required = true) Long evaluateId, @RequestParam(required = true) Long questionSetId) {
        if (evaluateId == null || questionSetId == null) {
            System.out.println("evaluateId or questionSetId is null.");
            return ResponseEntity.badRequest().body(false);
        }

        System.out.println("Received evaluateId: " + evaluateId);
        System.out.println("Received questionSetId: " + questionSetId);

        boolean result = evaluateService.addQuestionSet(evaluateId, questionSetId);
        if (result) {
            System.out.println("QuestionSet linked successfully!");
        } else {
            System.out.println("Failed to link QuestionSet.");
        }
        return ResponseEntity.ok(result);
    }


    @GetMapping("/list")
    public ResponseEntity<List<Evaluate>> listEvaluates() {
        return ResponseEntity.ok(evaluateService.list());
    }

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> listQuestionsByEvaluate(@RequestParam Long evaluateId) {
        return ResponseEntity.ok(evaluateService.listQuestion(evaluateId));
    }
}
