package DA.backend.controller;


import DA.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@CrossOrigin
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmailWithAttachment(
            @RequestParam("toEmail") String toEmail,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("userId") String userId,
            @RequestParam("objectName") String objectName) {

        String result = emailService.sendEmailWithAttachment(toEmail, subject, body, userId, objectName);
        return ResponseEntity.ok(result);
    }
}