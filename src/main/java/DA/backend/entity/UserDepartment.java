package DA.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_department")
@Data
public class UserDepartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}