package DA.backend.repository;

import DA.backend.entity.Meeting;
import DA.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    // Lấy danh sách cuộc họp chưa quá ngày hiện tại
    List<Meeting> findByEndTimeAfter(Date currentDate);

    // Lấy danh sách cuộc họp của user
    List<Meeting> findByParticipants_Id(String userId);
//    @Query("SELECT m FROM Meeting m ORDER BY m.id DESC")
    Meeting findTopByOrderByIdDesc();
}

