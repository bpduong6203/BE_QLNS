package DA.backend.service;

import DA.backend.entity.Question;
import DA.backend.entity.QuestionSet;
import DA.backend.repository.QuestionRepository;
import DA.backend.repository.QuestionSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionSetService {
    @Autowired
    QuestionSetRepository questionSetRepository;
    @Autowired
    QuestionRepository questionRepository;
    public boolean add(QuestionSet questionSet){
        try {
            questionSetRepository.save(questionSet);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
    public boolean update(QuestionSet questionSet){
        try {
            Optional<QuestionSet> optionalQuestionSet = questionSetRepository.findById(questionSet.getId());
            if(optionalQuestionSet.isPresent()){
                QuestionSet questionSet1 = optionalQuestionSet.get();
                questionSet1.setName(questionSet.getName());
                questionSetRepository.save(questionSet1);
                return true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
    public boolean addQuestion(Long id, List<Question> questions) {
        Optional<QuestionSet> optionalQuestionSet = questionSetRepository.findById(id);
        if (optionalQuestionSet.isPresent()) {
            QuestionSet questionSet = optionalQuestionSet.get();
            // Gộp danh sách câu hỏi
            questionSet.getQuestions().addAll(questions);
            questionSetRepository.save(questionSet);
            return true;
        }
        return false;
    }
    public boolean addQuestionByIds(Long questionSetId, List<Long> questionIds) {
        Optional<QuestionSet> optionalQuestionSet = questionSetRepository.findById(questionSetId);
        if (optionalQuestionSet.isPresent()) {
            QuestionSet questionSet = optionalQuestionSet.get();
            List<Question> questions = questionRepository.findAllById(questionIds); // Tìm câu hỏi từ ID
            questionSet.getQuestions().addAll(questions);
            questionSetRepository.save(questionSet);
            return true;
        }
        return false;
    }

    public boolean delete(Long id){
        Optional<QuestionSet> optionalQuestionSet = questionSetRepository.findById(id);
        if (optionalQuestionSet.isPresent()){
            QuestionSet questionSet = optionalQuestionSet.get();
            questionSetRepository.delete(questionSet);
            return true;
        }
        return false;
    }
    public List<QuestionSet> list(){
        return questionSetRepository.findAll();
    }
    public List<Question> listQuestionByQuestionSet(Long id) {
        Optional<QuestionSet> optionalQuestionSet = questionSetRepository.findById(id);
        if (optionalQuestionSet.isPresent()) {
            QuestionSet questionSet = optionalQuestionSet.get();
            return new ArrayList<>(questionSet.getQuestions());
        }
        return Collections.emptyList();
    }
    public boolean deleteQuestionByQuestionSet( Long questionSetId,Long questionId){
        Optional<QuestionSet> optionalQuestionSet = questionSetRepository.findById(questionSetId);
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        if (optionalQuestion.isPresent() && optionalQuestionSet.isPresent()){
            QuestionSet questionSet = optionalQuestionSet.get();
            Question question = optionalQuestion.get();
            questionSet.getQuestions().remove(question);
            questionSetRepository.save(questionSet);
            return true;
        }
        return false;
    }

}
