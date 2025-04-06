package DA.backend.service;

import DA.backend.entity.Activity;
import DA.backend.entity.Department;
import DA.backend.entity.User;
import DA.backend.entity.User_Activity;
import DA.backend.repository.ActivityRepository;
import DA.backend.repository.UserActivityRepository;
import DA.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private UserRepository userRepository;

    // Thêm hoạt động
    public Activity addActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    // Sửa hoạt động
    public Activity updateActivity(Long activityId, Activity activityDetails) {
        Optional<Activity> activity = activityRepository.findById(activityId);
        if (activity.isPresent()) {
            Activity existingActivity = activity.get();
            existingActivity.setActivityName(activityDetails.getActivityName());
            existingActivity.setDescription(activityDetails.getDescription());
            existingActivity.setLocation(activityDetails.getLocation());
            existingActivity.setStartDate(activityDetails.getStartDate());
            existingActivity.setEndDate(activityDetails.getEndDate());
            existingActivity.setParticipantsCount(activityDetails.getParticipantsCount());
            return activityRepository.save(existingActivity);
        }
        return null;
    }

    // Xóa hoạt động
    public void deleteActivity(Long activityId) {
        activityRepository.deleteById(activityId);
    }

    // Đăng ký người dùng tham gia hoạt động
    public User_Activity registerUserForActivity(String userId, Long activityId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Activity> activity = activityRepository.findById(activityId);

        if (user.isPresent() && activity.isPresent()) {
            // Kiểm tra xem người dùng đã đăng ký chưa
            Optional<User_Activity> existingRegistration = userActivityRepository.findByUserIdAndActivityId(userId, activityId);
            if (existingRegistration.isPresent()) {
                // Người dùng đã đăng ký trước đó
                return null; // Hoặc ném ra ngoại lệ
            }

            User_Activity userActivity = new User_Activity();
            userActivity.setUser(user.get());
            userActivity.setActivity(activity.get());
            userActivity.setStatus(false);  // Chưa được duyệt

            return userActivityRepository.save(userActivity);
        }
        return null;
    }

    // Lấy danh sách người dùng theo phòng ban và hoạt động và tham gia activityId
    public List<User> userListDepartmentByActivity(Long activityId, String currentUserId) {
        // 1. Lấy thông tin người dùng hiện tại
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Set<Department> currentUserDepartments = currentUser.getDepartment();  // Giả sử phòng ban là một Set

        // 2. Lấy danh sách người dùng tham gia vào activityId
        List<User_Activity> usersInActivity = userActivityRepository.findByActivityIdAndStatusTrue(activityId);

        // 3. Lọc danh sách người dùng có phòng ban trùng với người dùng hiện tại và tham gia activityId
        return usersInActivity.stream()
                .map(User_Activity::getUser) // Lấy thông tin User từ User_Activity
                .collect(Collectors.toList());
    }
    public List<User> getUsersByActivityId(Long activityId) {
        return userActivityRepository.findUsersByActivityId(activityId);
    }




    public User findUser(String id){
        return userRepository.findById(id).orElse(null);
    }

    public Activity findActivity(Long id){
        return activityRepository.findById(id).orElse(null);
    }

    // Lấy tất cả hoạt động
    public List<Activity> getAllActivitiesbyUSer() {
        // Lấy danh sách các User_Activity từ repository
        List<User_Activity> userActivities = userActivityRepository.findAll();

        // Tạo một Set chứa tất cả các ActivityID đã có trong User_Activity
        Set<Long> userActivityIds = userActivities.stream()
                .map(userActivity -> userActivity.getActivity().getId())  // Lấy ActivityID từ User_Activity
                .collect(Collectors.toSet());

        // Lọc các Activity không có trong userActivityIds
        return activityRepository.findAll()
                .stream()
                .filter(activity -> !userActivityIds.contains(activity.getId()))  // Lọc theo ActivityID
                .collect(Collectors.toList());
    }
    public List<Activity> getAllActivities() {
        return  activityRepository.findAll();
    }


    // Lấy tất cả người dùng đã đăng ký tham gia hoạt động
    public List<User> userListDepartmentByActivityFalseUnA(Long activityId, String currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Set<Department> currentUserDepartments = currentUser.getDepartment();

        // Lấy danh sách user_activity chưa được duyệt
        List<User_Activity> usersInActivity = userActivityRepository.findByActivityIdAndStatusFalse(activityId);

        // Lọc theo phòng ban
        return usersInActivity.stream().map(User_Activity::getUser)
                .filter(ua -> ua.getDepartment().stream()
                        .anyMatch(department -> currentUserDepartments.contains(department)))
                .collect(Collectors.toList());
    }

    // Trong ActivityService.java////////////////////////////////////////////


    // Duyệt xác nhận người dùng tham gia hoạt động
    public void approveUsersForActivity(Long activityId, List<String> userActivityIds) {
        // Chuyển List<String> sang List<Long>
       // List<Long> uaIds = userActivityIds.stream().map(Long::valueOf).collect(Collectors.toList());

        // Tìm các userActivity theo danh sách ID

        for(String user: userActivityIds){
           Optional<User_Activity> existingRegistration= userActivityRepository.findByUserIdAndActivityId(user,activityId);
           if(existingRegistration.isPresent()){
               User_Activity user_activity =existingRegistration.get();
               user_activity.setStatus(true);
               userActivityRepository.save(user_activity);
           }
        }
    }

    // Lấy danh sách người dùng theo phòng ban và hoạt động  đã đăng ký nhưng chưa xác nhận
    public List<User> userListDepartmentByActivityFalse(Long activityId, String currentUserId) {
        // 1. Lấy thông tin người dùng hiện tại
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Set<Department> currentUserDepartments = currentUser.getDepartment();  // Giả sử phòng ban là một Set

        // 2. Lấy danh sách người dùng tham gia vào activityId
        List<User_Activity> usersInActivity = userActivityRepository.findByActivityIdAndStatusFalse(activityId);

        // 3. Lọc danh sách người dùng có phòng ban trùng với người dùng hiện tại
        return usersInActivity.stream()
                .map(User_Activity::getUser) // Lấy thông tin User từ User_Activity
                .filter(user -> user.getDepartment().stream()  // Lọc theo phòng ban
                        .anyMatch(department -> currentUserDepartments.contains(department)))
                .collect(Collectors.toList());
    }
    // lấy danh sách người dùng đã được xác nhận đăng ký
    public List<User> getApprovedUserActivities(Long activityId, String currentUserId) {
        // Lấy thông tin người dùng hiện tại
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
      // Giả sử phòng ban là một Set

        // Lấy danh sách User_Activity đã được duyệt
        List<User_Activity> usersInActivity = userActivityRepository.findByActivityIdAndStatusTrue(activityId);

        // Lọc theo phòng ban
        return usersInActivity.stream().map(User_Activity::getUser).collect(Collectors.toList());
    }

}