package DA.backend.controller;

import DA.backend.entity.Meeting;
import DA.backend.entity.User;
import DA.backend.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/meetings")
@CrossOrigin
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    // Add a new video call (meeting)
    @PostMapping("/add-video/{userId}")
    public ResponseEntity<String> addVideo(@RequestBody Meeting meeting, @PathVariable String userId) {
        meetingService.addVideo(meeting);
        meetingService.addOneUser(userId);
        return new ResponseEntity<>("Video call added successfully", HttpStatus.CREATED);
    }


    // Delete a meeting by roomId
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMeeting(@PathVariable Long id) {
        meetingService.delete(id);
        return new ResponseEntity<>("Meeting deleted successfully", HttpStatus.OK);
    }

    // Add users to an existing meeting by roomId
    @PostMapping("/add-users/{roomId}")
    public ResponseEntity<String> addUsersToMeeting(@PathVariable Long roomId, @RequestParam List<String> userID) {
        meetingService.addUserMeeting(roomId, userID);
        return new ResponseEntity<>("Users added to meeting successfully", HttpStatus.OK);
    }

    // List all meetings for a specific user by userId
    @GetMapping("/list/{userId}")
    public ResponseEntity<Set<Meeting>> listMeetings(@PathVariable String userId) {
        Set<Meeting> meetings = meetingService.listMeeting(userId);
        if (meetings != null && !meetings.isEmpty()) {
            return new ResponseEntity<>(meetings, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/list-all")
    public ResponseEntity<List<Meeting>> listAllMeetings() {
        List<Meeting> allMeetings = meetingService.findAllMeetings();
        return new ResponseEntity<>(allMeetings, HttpStatus.OK);
    }
    // Update a meeting (meeting details)
    @PutMapping("/update")
    public ResponseEntity<String> updateMeeting(@RequestBody Meeting meeting) {
        meetingService.updateMeeting(meeting);
        return new ResponseEntity<>("Meeting updated successfully", HttpStatus.OK);
    }
}
