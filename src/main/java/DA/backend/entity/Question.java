package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String question;
    private int core;
    // Constructor nhận ID
    public Question(Long id) {
        this.id = id;
    }
    public List<QuestionSet> getQuestionSets() {
        return questionSets;
    }

    public void setQuestionSets(List<QuestionSet> questionSets) {
        this.questionSets = questionSets;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "Bo_Cau_Hoi",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "questionSet_id")
    )
    @JsonIgnore
    private List<QuestionSet> questionSets;

    public Question(Long id, String question, int core) {
        this.id = id;
        this.question = question;
        this.core = core;
    }

    public Question() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public int getCore() {
        return core;
    }

    public void setCore(int core) {
        this.core = core;
    }
    @OneToMany(mappedBy = "question")
    @JsonIgnore
    private List<UserEvaluate> userEvaluates;

    public List<UserEvaluate> getUserEvaluates() {
        return userEvaluates;
    }

    public void setUserEvaluates(List<UserEvaluate> userEvaluates) {
        this.userEvaluates = userEvaluates;
    }
}
