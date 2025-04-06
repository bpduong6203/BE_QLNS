package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "user_evaluate")
public class UserEvaluate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "userAdminId", nullable = true)
    private User userAdmin;

    @ManyToOne
    @JoinColumn(name = "userManager", nullable = true)
    private User userManager;

    @ManyToOne
    @JoinColumn(name = "evaluateId", nullable = false)
    private Evaluate evaluate;

    @ManyToOne
    @JoinColumn(name = "questionId", nullable = false)
    private Question question;

    @Column(name = "score", nullable = true)
    private Integer score ;

    @Column(name = "score_admin", nullable = true)
    private Integer scoreAdmin;

    @Column(name = "score_manager", nullable = true)
    private Integer scoreManager;

    // Constructors
    public UserEvaluate() {}

    public UserEvaluate(User user, User userAdmin, User userManager, Evaluate evaluate, Question question) {
        this.user = user;
        this.userAdmin = userAdmin;
        this.userManager = userManager;
        this.evaluate = evaluate;
        this.question = question;

        // Lấy điểm từ câu hỏi
        Integer questionCore = question.getCore();
        System.out.println("Question Core: " + questionCore);  // In giá trị điểm câu hỏi ra để kiểm tra

        // Gán điểm cho từng trường
        this.score = questionCore;        // Điểm của người dùng
        this.scoreAdmin = questionCore;   // Điểm của admin
        this.scoreManager = questionCore; // Điểm của manager
    }



    // Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUserAdmin() {
        return userAdmin;
    }

    public void setUserAdmin(User userAdmin) {
        this.userAdmin = userAdmin;
    }

    public User getUserManager() {
        return userManager;
    }

    public void setUserManager(User userManager) {
        this.userManager = userManager;
    }

    public Evaluate getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(Evaluate evaluate) {
        this.evaluate = evaluate;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getScoreAdmin() {
        return scoreAdmin;
    }

    public void setScoreAdmin(Integer scoreAdmin) {
        this.scoreAdmin = scoreAdmin;
    }

    public Integer getScoreManager() {
        return scoreManager;
    }

    public void setScoreManager(Integer scoreManager) {
        this.scoreManager = scoreManager;
    }
}
