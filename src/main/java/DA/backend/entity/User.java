package DA.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.format.annotation.DateTimeFormat;


import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {
    @Id
    private String id;
    @NotBlank(message = "Tên không được bỏ trống")
    @Size(min = 1, max = 50, message = "Tên nằm trong khoảng 1 đến 50 ký tự")
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDay;

    private String nationality;
    private String homeTown;
    private String address;

    @NotBlank(message = "Email không được bỏ trống")
    @Size(min = 1, max = 50, message = "Email nằm trong khoảng 1 đến 50 ký tự")
    @Email
    private String email;

    @Length(min = 10, max = 10, message = "Số điện thoại phải đủ 10 số")
    @Pattern(regexp = "^[0-9]*$", message = "Số điện thoại phải nhập chữ số")
    private String phoneNumber;

    @Lob
    @Column(columnDefinition = "LONGTEXT") // Hibernate sẽ sử dụng kiểu LONGTEXT trong MySQL
    private String image;

    private String sex;
    private String password;
    private boolean isDelete = false;

// Inside User.java



    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<User_Activity> getUserActivities() {
        return userActivities;
    }

    public void setUserActivities(Set<User_Activity> userActivities) {
        this.userActivities = userActivities;
    }


    @ManyToMany
    @JoinTable(
            name = "meeting_user",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "meeting_id"))
    private Set<Meeting> meetings;

    public User(String id, String name, Date birthDay, String nationality, String homeTown, String address, String email, String phoneNumber, String image, String sex, String password, boolean isDelete) {
        this.id = id;
        this.name = name;
        this.birthDay = birthDay;
        this.nationality = nationality;
        this.homeTown = homeTown;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.image = image;
        this.sex = sex;
        this.password = password;
        this.isDelete = isDelete;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @ManyToOne
     @JoinColumn(name = "position_id")
     @JsonIgnore
     private Position position;

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }


    public Set<Department> getDepartment() {
        return department;
    }

    public void setDepartment(Set<Department> department) {
        this.department = department;
    }

    @ManyToMany
    @JoinTable(
            name = "user_department",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"))
    @JsonIgnore
    private Set<Department> department;
    // nối
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonManagedReference
    @JsonIgnore
    private Set<Role> roles;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
 public User() {
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getHomeTown() {
        return homeTown;
    }

    public void setHomeTown(String homeTown) {
        this.homeTown = homeTown;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

   @OneToMany(mappedBy = "user")
   @JsonIgnore
    private List<UserEvaluate> userEvaluates;


    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<User_Activity> userActivities; // Liên kết với bảng User_Activity

    public List<UserEvaluate> getUserEvaluates() {
        return userEvaluates;
    }

    public void setUserEvaluates(List<UserEvaluate> userEvaluates) {
        this.userEvaluates = userEvaluates;
    }

    // nối chấm công
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Attendance> attendances;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<AttendanceCode> attendanceCodes;

    @OneToMany(mappedBy = "requestedBy")
    @JsonIgnore
    private List<AttendanceModificationRequest> modificationRequests;
}
