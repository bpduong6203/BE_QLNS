package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
@Entity
@Table(name = "user_activity")
public class User_Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean status; // Sửa lỗi chính tả từ "tatus" thành "status"

    @ManyToOne
    @JoinColumn(name = "activity_id") // Cột khóa ngoại trỏ đến Activity
    @JsonIgnore
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "user_id") // Cột khóa ngoại trỏ đến User
    @JsonIgnore
    private User user;

    // Constructors, getters and setters
    public User_Activity() {}

    public User_Activity(Long id, boolean status, Activity activity, User user) {
        this.id = id;
        this.status = status;
        this.activity = activity;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
