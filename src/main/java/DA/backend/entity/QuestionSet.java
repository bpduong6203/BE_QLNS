package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;


@Entity
@Table(name = "questionSet")
public class QuestionSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;


    @OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonIgnore
    private List<Evaluate> evaluates;


    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "Bo_Cau_Hoi",
            joinColumns = @JoinColumn(name = "questionSet_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @JsonIgnore
    private List<Question> questions;

    public QuestionSet() {
    }

    public QuestionSet(Long id, String name) {
        this.id = id;
        this.name = name;
    }

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


}
