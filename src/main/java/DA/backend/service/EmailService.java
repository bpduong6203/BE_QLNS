package DA.backend.service;

import DA.backend.entity.IdGenerator;
import DA.backend.entity.User;
import DA.backend.repository.UserRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

@Service
public class EmailService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;

    public String Code;

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
    public void sendCode(String id, String email, String subject){
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isPresent()){
            Code = IdGenerator.generateUniqueId();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(Code);
            mailSender.send(message);
        }

    }
    @Autowired
    private MinioClient minioClient;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public String sendEmailWithAttachment(String toEmail, String subject, String body, String userId, String objectName) {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket("bucket-" + userId)
                .object(objectName)
                .build())) {

            // Tạo email với đính kèm file
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body);

            // Đính kèm file
            ByteArrayResource resource = new ByteArrayResource(stream.readAllBytes());
            helper.addAttachment(objectName, resource);

            // Gửi email
            mailSender.send(message);
            return "Email sent successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send email: " + e.getMessage();
        }
    }
    public void sendHtmlEmail(String to, String subject, String htmlContent, byte[] qrCodeBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set email details
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // Embed QR Code
            ByteArrayResource qrImageResource = new ByteArrayResource(qrCodeBytes);
            helper.addInline("qrCodeImage", qrImageResource, "image/png");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error sending email", e);
        }
    }
}