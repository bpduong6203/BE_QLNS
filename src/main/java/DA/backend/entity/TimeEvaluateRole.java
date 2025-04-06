package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "time_evaluate_role")
public class TimeEvaluateRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private Date startDay;

    @Temporal(TemporalType.DATE)
    private Date endDay;

    @ManyToOne
    @JoinColumn(name = "role_id",nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "evaluate_id", nullable = false)
    private Evaluate evaluate;

    public TimeEvaluateRole() {}


    public TimeEvaluateRole(Long id, Date startDay, Date endDay, Role role, Evaluate evaluate) {
        this.id = id;
        this.startDay = startDay;
        this.endDay = endDay;
        this.role = role;
        this.evaluate = evaluate;
    }

    // Getters và setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDay() {
        return startDay;
    }

    public void setStartDay(Date startDay) {
        this.startDay = startDay;
    }

    public Date getEndDay() {
        return endDay;
    }

    public void setEndDay(Date endDay) {
        this.endDay = endDay;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Evaluate getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(Evaluate evaluate) {
        this.evaluate = evaluate;
    }
}
