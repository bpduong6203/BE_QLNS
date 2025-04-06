package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;
@Entity
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Activity ID

    private String activityName;  // Activity Name
    private String description;  // Description
    private String location;  // Location
    private String startDate;  // Start Date
    private String endDate;  // End Date
    private int participantsCount;  // Participants Count

    // Quan hệ OneToMany với bảng trung gian User_Activity
    @OneToMany(mappedBy = "activity")
    @JsonIgnore
    private Set<User_Activity> userActivities; // Thay đổi từ "activity" thành "userActivities" vì đây là quan hệ 1-n với bảng trung gian

    // Constructors, getters, and setters
    public Activity() {}


    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Activity(Long id, String endDate, String startDate, String location, String activityName, String description, int participantsCount) {
        this.id = id;
        this.endDate = endDate;
        this.startDate = startDate;
        this.location = location;
        this.activityName = activityName;
        this.description = description;
        this.participantsCount = participantsCount;
    }

    public int getParticipantsCount() {
        return participantsCount;
    }

    public void setParticipantsCount(int participantsCount) {
        this.participantsCount = participantsCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getters and Setters
    public Set<User_Activity> getUserActivities() {
        return userActivities;
    }

    public void setUserActivities(Set<User_Activity> userActivities) {
        this.userActivities = userActivities;
    }

    // Các getter/setter khác...
}
