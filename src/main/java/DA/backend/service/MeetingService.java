package DA.backend.service;

import DA.backend.controller.MeetingController;
import DA.backend.entity.Meeting;
import DA.backend.entity.User;
import DA.backend.repository.MeetingRepository;
import DA.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    // Thêm mới video call
    public void addVideo(Meeting meeting) {

                meetingRepository.save(meeting);
    }
    public void addOneUser( String userID){
        Meeting meeting= meetingRepository.findTopByOrderByIdDesc();
                Optional<User> optionalUser = userRepository.findById(userID);
                if(optionalUser.isPresent()){
                    User user1 = optionalUser.get();
                    user1.getMeetings().add(meeting);
                    meetingRepository.save(meeting);
                }

    }
    public void delete(Long id){
       meetingRepository.deleteById(id);
   }
    public void addUserMeeting(Long roomId, List<String> userID){
        Optional<Meeting> optionalMeeting = meetingRepository.findById(roomId);
        if(optionalMeeting.isPresent() ){
            Meeting meeting = optionalMeeting.get();
            for(String user: userID ){
                Optional<User> optionalUser = userRepository.findById(user);
                if(optionalUser.isPresent()){
                    User user1 = optionalUser.get();
                    meeting.getParticipants().add(user1);
                    meetingRepository.save(meeting);
                }
            }

        }
    }
   public Set<Meeting> listMeeting(String userId){
       Optional<User> optionalUser = userRepository.findById(userId);
       if (optionalUser.isPresent()){
           User user = optionalUser.get();
           return new HashSet<>(meetingRepository.findAll()
                   .stream()
                   .filter(meeting -> meeting.getParticipants().contains(user))
                   .collect(Collectors.toList()));
       }
       return  null;

   }
   public void updateMeeting(Meeting meeting){
       Optional<Meeting> optionalMeeting = meetingRepository.findById(meeting.getRoomID());
       if(optionalMeeting.isPresent()){
           Meeting meeting1 = optionalMeeting.get();
           meeting1.setParticipants(meeting.getParticipants());
           meeting1.setMeetingName(meeting.getMeetingName());
           meeting1.setDescription(meeting.getDescription());
           meeting1.setEndTime(meeting.getEndTime());
           meeting1.setStartTime(meeting.getStartTime());
           meetingRepository.save(meeting1);
       }
   }

    public List<Meeting> findAllMeetings() {
        return meetingRepository.findAll();
    }

}

