package DA.backend.controller;

import DA.backend.entity.Question;
import DA.backend.repository.QuestionRepository;
import DA.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/questions")
@CrossOrigin
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/add")
    public ResponseEntity<Boolean> addQuestion(@RequestBody Question question) {
        return ResponseEntity.ok(questionService.add(question));
    }

    @PutMapping("/update")
    public ResponseEntity<Boolean> updateQuestion(@RequestBody Question question) {
        return ResponseEntity.ok(questionService.update(question));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Question>> listQuestions() {
        return ResponseEntity.ok(questionService.questionList());
    }
}
