package DA.backend.service;

import DA.backend.entity.Question;
import DA.backend.entity.UserEvaluate;
import DA.backend.repository.QuestionRepository;
import DA.backend.repository.UserEvaluateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    private UserEvaluateRepository userEvaluateRepository;
    public boolean add(Question question) {
        try {
            questionRepository.save(question);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean update(Question question) {
        try {
            Optional<Question> optionalQuestion = questionRepository.findById(question.getId());
            if (optionalQuestion.isPresent()) {
                Question question1 = optionalQuestion.get();
                question1.setQuestion(question.getQuestion());
                question1.setCore(question.getCore()); // Cập nhật điểm câu hỏi

                // Cập nhật điểm cho tất cả các UserEvaluate liên quan, nhưng chỉ nếu họ đã đánh giá
                List<UserEvaluate> userEvaluates = userEvaluateRepository.findByQuestionId(question.getId());
                for (UserEvaluate userEvaluate : userEvaluates) {
                    // Chỉ cập nhật điểm nếu người dùng đã đánh giá
                    if (userEvaluate.getScore() != null) {  // Nếu đã có điểm từ người dùng
                        userEvaluate.setScore(question.getCore());  // Cập nhật điểm user
                    }
                    if (userEvaluate.getScoreAdmin() != null) {  // Nếu admin đã đánh giá
                        userEvaluate.setScoreAdmin(question.getCore());  // Cập nhật điểm admin
                    }
                    if (userEvaluate.getScoreManager() != null) {  // Nếu manager đã đánh giá
                        userEvaluate.setScoreManager(question.getCore());  // Cập nhật điểm manager
                    }

                    userEvaluateRepository.save(userEvaluate);  // Lưu các bản ghi UserEvaluate sau khi cập nhật điểm
                }

                questionRepository.save(question1);  // Lưu lại câu hỏi sau khi cập nhật
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }



    public List<Question> questionList(){
        return questionRepository.findAll();
    }
}
