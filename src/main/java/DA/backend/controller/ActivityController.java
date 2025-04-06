package DA.backend.controller;

import DA.backend.entity.Activity;
import DA.backend.entity.IdGenerator;
import DA.backend.entity.User;
import DA.backend.entity.User_Activity;
import DA.backend.service.ActivityService;
import DA.backend.service.EmailService;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin
public class ActivityController {

    @Autowired
    private ActivityService activityService;
    @Autowired
    private EmailService emailService;

    @GetMapping("/{activityId}/users")
    public ResponseEntity<List<User>> getUsersByActivityId(@PathVariable Long activityId) {
        List<User> users = activityService.getUsersByActivityId(activityId);
        return ResponseEntity.ok(users);
    }

    // Thêm hoạt động
    @PostMapping
    public ResponseEntity<Activity> createActivity(@RequestBody Activity activity) {
        Activity createdActivity = activityService.addActivity(activity);
        return ResponseEntity.ok(createdActivity);
    }

    // Sửa hoạt động
    @PutMapping("/{activityId}")
    public ResponseEntity<Activity> updateActivity(@PathVariable Long activityId, @RequestBody Activity activityDetails) {
        Activity updatedActivity = activityService.updateActivity(activityId, activityDetails);
        if (updatedActivity != null) {
            return ResponseEntity.ok(updatedActivity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Xóa hoạt động
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long activityId) {
        activityService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    // Đăng ký người dùng tham gia hoạt động
    @PostMapping("/register/{userId}/{activityId}")
    public ResponseEntity<User_Activity> registerUserForActivity(@PathVariable String userId, @PathVariable Long activityId) {
        User_Activity userActivity = activityService.registerUserForActivity(userId, activityId);
        if (userActivity != null) {
            return ResponseEntity.ok(userActivity);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    // Trong service hoặc controller, thay vì trả về List<User>, ta trả về List<User_Activity>
    @GetMapping("/department/{activityId}")
    public ResponseEntity<List<User>> getUsersByDepartmentAndActivity(
            @PathVariable Long activityId,
            @RequestParam String userId) {
        try {
            List<User> userActivities = activityService.userListDepartmentByActivityFalseUnA(activityId, userId);
            return ResponseEntity.ok(userActivities);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Lấy tất cả người dùng đã đăng ký tham gia hoạt động
//    @GetMapping("/{activityId}/users")
//    public ResponseEntity<List<User_Activity>> getAllUsersByActivity(@PathVariable Long activityId) {
//        List<User_Activity> users = activityService.getAllUsersByActivity(activityId);
//        return ResponseEntity.ok(users);
//    }

    // Duyệt xác nhận người dùng tham gia hoạt động
    @PutMapping("/{activityId}/approve")
    public ResponseEntity<Void> approveUsersForActivity(@PathVariable Long activityId, @RequestBody List<String> userActivityIds) {
        activityService.approveUsersForActivity(activityId, userActivityIds);
        for(String id: userActivityIds){
            User user =  activityService.findUser(id);
            Activity activity = activityService.findActivity(activityId);
            if(user != null && activity != null)
                emailService.sendEmail(user.getEmail(),"HOẠT ĐỘNG","Thông tin Hoạt động "+"Tên hoạt đông:"+activity.getActivityName() + "Mô tả:"+activity.getDescription() + "Thời gian bắt đầu: "+ activity.getStartDate() + "Thời gian kết thúc: "+ activity.getEndDate() + "Địa điểm: "+activity.getLocation());
        }

        return ResponseEntity.ok().build();
    }
    // Bổ sung phương thức lấy tất cả hoạt động
    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities() {
        List<Activity> activities = activityService.getAllActivities();
        return ResponseEntity.ok(activities);
    }
    @GetMapping("/acivityUserRegister")
    public ResponseEntity<List<Activity>> getAllActivitiesUser() {
        List<Activity> activities = activityService.getAllActivitiesbyUSer();
        return ResponseEntity.ok(activities);
    }
    @GetMapping("/department/approve/{activityId}")
    public ResponseEntity<List<User>> getApprovedUserActivities(
            @PathVariable Long activityId,
            @RequestParam String userId) {
        try {
            List<User> userActivities = activityService.getApprovedUserActivities(activityId, userId);
            return ResponseEntity.ok(userActivities);  // Trả về danh sách User_Activity
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);  // Nếu có lỗi xảy ra
        }
    }


}