package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.Year;
import java.util.Set;

@Entity
@Table(name = "position")
public class Position {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(min= 1, max = 50, message = "Tên chức vụ không được bỏ trống")
    private String name;

    @OneToMany(mappedBy = "position",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<User> users;
    public Position(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    public Position() {
    }



    public @Size(min = 1, max = 50, message = "Tên chức vụ không được bỏ trống") String getName() {
        return name;
    }

    public void setName(@Size(min = 1, max = 50, message = "Tên chức vụ không được bỏ trống") String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }




}
