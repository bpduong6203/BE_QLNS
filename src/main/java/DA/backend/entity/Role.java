package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.List;


@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên không được bỏ trống")
    @Column(name = "name", length = 50, nullable = false)
    @Size(max = 50, message = "Tên phải nhỏ hơn 50 ký tự")
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonBackReference
    @JsonIgnore
    private List<User> users;

    @OneToMany(mappedBy = "role")
    @JsonIgnore
    private List<TimeEvaluateRole> timeEvaluateRoles;

    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    // Getters và setters
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

    public List<TimeEvaluateRole> getTimeEvaluateRoles() {
        return timeEvaluateRoles;
    }

    public void setTimeEvaluateRoles(List<TimeEvaluateRole> timeEvaluateRoles) {
        this.timeEvaluateRoles = timeEvaluateRoles;
    }
}
