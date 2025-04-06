package DA.backend.service;

import DA.backend.entity.*;
import DA.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EvaluateService {

    @Autowired
    EvaluateRepository evaluateRepository;
    @Autowired
    QuestionSetRepository questionSetRepository;

    public boolean add(Evaluate evaluate) {
        try {
            // Kiểm tra xem QuestionSet có null hay không
            if (evaluate.getQuestionSet() != null) {
                // Nếu QuestionSet chưa tồn tại, lưu vào database trước
                QuestionSet questionSet = evaluate.getQuestionSet();
                if (questionSet.getId() == null) {
                    // Nếu QuestionSet chưa có ID (chưa lưu), lưu QuestionSet vào DB
                    questionSetRepository.save(questionSet);
                }
            }

            // Lưu Evaluate cùng với QuestionSet đã có
            Evaluate evaluate1 = new Evaluate();
            evaluate1.setQuestionSet(evaluate.getQuestionSet());
            evaluate1.setYear(evaluate.getYear());
            evaluate1.setName(evaluate.getName());
            evaluateRepository.save(evaluate1); // Lưu Evaluate

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    public boolean update(Evaluate evaluate) {
        Optional<Evaluate> optionalEvaluate = evaluateRepository.findById(evaluate.getId());
        if (optionalEvaluate.isPresent()) {
            Evaluate existingEvaluate = optionalEvaluate.get();
            existingEvaluate.setName(evaluate.getName());
            existingEvaluate.setYear(evaluate.getYear());
            evaluateRepository.save(existingEvaluate);
            return true;
        }
        return false;
    }

    public boolean addQuestionSet(Long evaluateId, Long questionSetId) {
        Optional<Evaluate> optionalEvaluate = evaluateRepository.findById(evaluateId);
        Optional<QuestionSet> optionalQuestionSet = questionSetRepository.findById(questionSetId);

        if (optionalEvaluate.isEmpty() || optionalQuestionSet.isEmpty()) {
            System.out.println("Evaluate or QuestionSet not found.");
            return false;
        }

        Evaluate evaluate = optionalEvaluate.get();
        QuestionSet questionSet = optionalQuestionSet.get();

        System.out.println("Linking QuestionSet ID " + questionSetId + " to Evaluate ID " + evaluateId);
        evaluate.setQuestionSet(questionSet); // Correcting the method name to match the entity's field
        evaluateRepository.save(evaluate);
        return true;
    }


    public List<Evaluate> list() {
        return evaluateRepository.findAll();
    }

    public List<Question> listQuestion(Long evaluateId) {
        Optional<Evaluate> optionalEvaluate = evaluateRepository.findById(evaluateId);
        if (optionalEvaluate.isPresent()) {
            Evaluate evaluate = optionalEvaluate.get();
            if (evaluate.getQuestionSets() != null) {
                return evaluate.getQuestionSets().getQuestions();
            }
        }
        return null;
    }
}
