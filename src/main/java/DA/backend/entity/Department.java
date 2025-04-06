package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;


@Entity
@Table(name = "department")
public class Department {
    public Department(String name, Long id) {
        this.name = name;
        this.id = id;
    }

    public Department() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 1, max = 50, message = "Tên phòng ban không được bỏ trông")
    private String name;


    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
// cascade = {CascadeType.PERSIST, CascadeType.MERGE}  tự động lưu và ánh xạ ngược lại
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_department",
            joinColumns = @JoinColumn(name = "department_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnore
    private Set<User> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @Size(min = 1, max = 50, message = "Tên phòng ban không được bỏ trông") String getName() {
        return name;
    }

    public void setName(@Size(min = 1, max = 50, message = "Tên phòng ban không được bỏ trông") String name) {
        this.name = name;
    }

//    public List<User> getUsers() {
//        return users;
//    }
//
//    public void setUsers(List<User> users) {
//        this.users = users;
//    }


}
