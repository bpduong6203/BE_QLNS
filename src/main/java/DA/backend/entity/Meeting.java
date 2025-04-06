package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "meeting")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Đặt roomID là khóa chính

    @NotBlank(message = "Tên cuộc họp không được bỏ trống")
    private String meetingName;

    public Long getRoomID() {
        return id;
    }

    public void setRoomID(Long roomID) {
        this.id = roomID;
    }

    public @NotBlank(message = "Tên cuộc họp không được bỏ trống") String getMeetingName() {
        return meetingName;
    }

    public void setMeetingName(@NotBlank(message = "Tên cuộc họp không được bỏ trống") String meetingName) {
        this.meetingName = meetingName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    public Meeting() {
    }

    public Meeting(Long roomID, String meetingName, Date startTime, Date endTime, String description) {
        this.id = roomID;
        this.meetingName = meetingName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }


    private Date startTime;

    private Date endTime;

    private String description;

    @ManyToMany
    @JoinTable(
            name = "meeting_user",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnore
    private Set<User> participants;



}
