package DA.backend.controller;

import DA.backend.dto.RemainingLeaveDTO;
import DA.backend.entity.Leave;
import DA.backend.service.LeaveService;
import DA.backend.service.UserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin
public class LeaveController {

    private static final Logger logger = LoggerFactory.getLogger(LeaveController.class);
    @Autowired
    private LeaveService leaveService;
    @Autowired
    private UserService userService;

    // Lấy danh sách đơn xin nghỉ của user
    @GetMapping("/my-requests")
    public ResponseEntity<List<Leave>> getMyLeaveRequests(
            @RequestParam String userId) {
        try {
            List<Leave> leaves = leaveService.getLeavesByUserId(userId);
            return ResponseEntity.ok(leaves);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Xem số ngày nghỉ còn lại
    @GetMapping("/remaining-days")
    public ResponseEntity<RemainingLeaveDTO> getRemainingLeaveDays(
            @RequestParam String userId) {
        try {
            RemainingLeaveDTO remainingDays = leaveService.getRemainingLeaveDays(userId);
            return ResponseEntity.ok(remainingDays);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Tạo đơn xin nghỉ
    @PostMapping(value = "/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createLeaveRequest(
            @RequestPart("leave") String leaveJson,
            @RequestPart(value = "evidence", required = false) MultipartFile evidenceFile
    ) {
        try {
            logger.info("Received leave request data: {}", leaveJson);
            if (evidenceFile != null) {
                logger.info("Evidence file: name={}, type={}, size={}",
                        evidenceFile.getOriginalFilename(),
                        evidenceFile.getContentType(),
                        evidenceFile.getSize());
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            Leave leave = mapper.readValue(leaveJson, Leave.class);

            if (leave.getUser() == null || leave.getUser().getId() == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }

            Leave createdLeave = leaveService.createLeaveWithEvidence(leave, evidenceFile);
            return ResponseEntity.ok(createdLeave);
        } catch (Exception e) {
            logger.error("Error creating leave request", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Hủy đơn xin nghỉ
    @PostMapping("/{leaveId}/cancel")
    public ResponseEntity<?> cancelLeaveRequest(
            @PathVariable Long leaveId,
            @RequestHeader(value = "user-id", required = false) String userId
    ) {
        try {
            Leave cancelledLeave = leaveService.cancelLeave(leaveId);
            return ResponseEntity.ok(cancelledLeave);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error cancelling leave request: " + e.getMessage());
        }
    }
}