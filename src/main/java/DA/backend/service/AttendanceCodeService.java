package DA.backend.service;

import DA.backend.entity.AttendanceCode;
import DA.backend.entity.User;
import DA.backend.enums.AttendanceCodeType;
import DA.backend.repository.AttendanceCodeRepository;
import DA.backend.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AttendanceCodeService {

    @Autowired
    private AttendanceCodeRepository attendanceCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Thời gian hiệu lực của mã (15 phút)
    private static final int CODE_VALIDITY_MINUTES = 15;

    // Tạo mã QR dạng Base64 string
    private String generateQRCodeBase64(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrCodeBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR Code", e);
        }
    }


    // Tạo mã chấm công và gửi email
    public AttendanceCode generateCode(String userId, AttendanceCodeType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate random 6-digit code
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        AttendanceCode attendanceCode = new AttendanceCode();
        attendanceCode.setUser(user);
        attendanceCode.setCode(code);
        attendanceCode.setType(type);
        attendanceCode.setExpirationTime(LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES));

        attendanceCode = attendanceCodeRepository.save(attendanceCode);

        // Generate QR code as byte array
        String qrContent = String.format("%s:%s:%s", userId, code, type);
        byte[] qrCodeBytes = generateQRCodeBytes(qrContent);

        // Email content
        String subject = type.equals("CHECK_IN") ? "Mã check-in" : "Mã check-out";
        String htmlContent = String.format(
                "<html><body>" +
                        "<h2>Mã %s của bạn</h2>" +
                        "<p>Mã số: <strong>%s</strong></p>" +
                        "<p>Mã có hiệu lực trong %d phút</p>" +
                        "<p>Bạn có thể nhập mã trên hoặc quét mã QR dưới đây:</p>" +
                        "<img src='cid:qrCodeImage' alt='QR Code'/>" +
                        "</body></html>",
                type.name().toLowerCase(), code, CODE_VALIDITY_MINUTES
        );

        // Send email
        emailService.sendHtmlEmail(user.getEmail(), subject, htmlContent, qrCodeBytes);

        return attendanceCode;
    }

    private byte[] generateQRCodeBytes(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR Code", e);
        }
    }

    // Xác thực mã
    public boolean validateCode(String userId, String code, AttendanceCodeType type) {
        return attendanceCodeRepository.findByUserIdAndCodeAndTypeAndIsUsedFalse(userId, code, type)
                .filter(attendanceCode ->
                        attendanceCode.getExpirationTime().isAfter(LocalDateTime.now()))
                .map(attendanceCode -> {
                    attendanceCode.setUsed(true);
                    attendanceCode.setUsedAt(LocalDateTime.now());
                    attendanceCodeRepository.save(attendanceCode);
                    return true;
                })
                .orElse(false);
    }

    // Xóa mã hết hạn (chạy mỗi giờ)
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredCodes() {
        attendanceCodeRepository.deleteByExpirationTimeBefore(LocalDateTime.now());
    }

    // Tạo mã QR
    public String generateQRContent(String userId, AttendanceCodeType type) {
        AttendanceCode code = generateCode(userId, type);
        // Format: userId:code:type
        return String.format("%s:%s:%s", userId, code.getCode(), type);
    }
}