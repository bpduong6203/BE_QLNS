package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Year;
import java.util.List;

@Entity
@Table(name = "evaluate")
public class Evaluate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Year year;

    @ManyToOne
    @JoinColumn(name = "question_set_id")
    private QuestionSet questionSet;

    @OneToMany(mappedBy = "evaluate")
    @JsonIgnore
    private List<UserEvaluate> userEvaluates;

    @OneToMany(mappedBy = "evaluate")
    @JsonIgnore
    private List<TimeEvaluateRole> timeEvaluateRoles;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
    }

    public QuestionSet getQuestionSets() {
        return questionSet;
    }

    public void setQuestionSets(QuestionSet questionSet) {
        this.questionSet = questionSet;
    }

    public List<UserEvaluate> getUserEvaluates() {
        return userEvaluates;
    }

    public void setUserEvaluates(List<UserEvaluate> userEvaluates) {
        this.userEvaluates = userEvaluates;
    }

    public List<TimeEvaluateRole> getTimeEvaluateRoles() {
        return timeEvaluateRoles;
    }

    public void setTimeEvaluateRoles(List<TimeEvaluateRole> timeEvaluateRoles) {
        this.timeEvaluateRoles = timeEvaluateRoles;
    }

    public QuestionSet getQuestionSet() {
        return questionSet;
    }

    public void setQuestionSet(QuestionSet questionSet) {
        this.questionSet = questionSet;
    }
}
