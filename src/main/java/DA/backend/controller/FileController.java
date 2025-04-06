package DA.backend.controller;

import DA.backend.service.EmailService;
import DA.backend.service.LockService;
import DA.backend.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    @Autowired
    private MinioService minIOService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private EmailService emailService;
    @Autowired
    private LockService lockService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId) {
        String result = minIOService.uploadFile(file, userId);

        Map<String, String> message = new HashMap<>();
        message.put("action", "upload");
        message.put("fileName", file.getOriginalFilename());

        messagingTemplate.convertAndSend("/topic/files", message);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/files/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileMetadataDTO>> getFilesFromMinIO(@PathVariable("userId") String userId) {
        List<FileMetadataDTO> fileDTOs = minIOService.listFilesFromMinIO(userId);
        return ResponseEntity.ok(fileDTOs);
    }

    @GetMapping("/download/{userId}/{objectName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String userId, @PathVariable String objectName) {
        byte[] content = minIOService.downloadFile(userId, objectName);
        if (content == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                .body(content);
    }

    @DeleteMapping("/delete/{userId}/{objectName}")
    public ResponseEntity<String> deleteFile(@PathVariable String userId, @PathVariable String objectName) {
        String result = minIOService.deleteFile(userId, objectName);

        Map<String, String> message = new HashMap<>();
        message.put("action", "delete");
        message.put("fileName", objectName);

        messagingTemplate.convertAndSend("/topic/files", message);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/share/link")
    public ResponseEntity<String> shareFileLink(
            @RequestParam("userId") String userId,
            @RequestParam("fileName") String fileName,
            @RequestParam("permission") String permission,
            @RequestParam("expirationDate") @DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm:ss") Date expirationDate) {

        String token = minIOService.createShareableLink(userId, fileName, permission, expirationDate);
        return ResponseEntity.ok("https://backend-ard2.onrender.com/api/file/access/" + token);
    }

    @PostMapping("/share/email")
    public ResponseEntity<String> shareFileViaEmail(
            @RequestParam("toEmail") String toEmail,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam("userId") String userId,
            @RequestParam("fileName") String fileName) {
        String result = emailService.sendEmailWithAttachment(toEmail, subject, body, userId, fileName);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/access/{token}")
    public ResponseEntity<byte[]> accessSharedFile(@PathVariable String token) {
        try {
            byte[] fileContent = minIOService.accessSharedFile(token);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileContent);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }
    // Viewing endpoints
    @GetMapping("/view/html/{userId}/{objectName}")
    public ResponseEntity<String> viewFileAsHtml(@PathVariable String userId, @PathVariable String objectName) {
        String htmlContent = minIOService.convertDocxToHtml(userId, objectName);
        if (htmlContent == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to convert file.");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlContent);
    }

    @GetMapping("/view/pdf/{userId}/{objectName}")
    public ResponseEntity<byte[]> viewFileAsPdf(@PathVariable String userId, @PathVariable String objectName) {
        byte[] pdfContent = minIOService.convertDocxToPdf(userId, objectName);
        if (pdfContent == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    // Editing endpoints
    @PostMapping("/edit/url/{userId}/{objectName}")
    public ResponseEntity<Map<String, String>> getEditUrl(@PathVariable String userId, @PathVariable String objectName) {
        // Implement OnlyOffice or your chosen editor integration here
        // Example response:
        String editUrl = minIOService.generateEditUrl(userId, objectName);
        Map<String, String> response = new HashMap<>();
        response.put("editUrl", editUrl);
        return ResponseEntity.ok(response);
    }

    // Locking endpoints
    @PostMapping("/edit/lock")
    public ResponseEntity<String> lockFile(@RequestParam String userId, @RequestParam String objectName, @RequestParam String editorId) {
        boolean acquired = lockService.acquireLock(userId, objectName, editorId);
        if (acquired) {
            return ResponseEntity.ok("Lock acquired.");
        } else {
            String currentEditor = lockService.getEditor(userId, objectName);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("File is already being edited by " + currentEditor);
        }
    }

    @PostMapping("/edit/unlock")
    public ResponseEntity<String> unlockFile(@RequestParam String userId, @RequestParam String objectName, @RequestParam String editorId) {
        lockService.releaseLock(userId, objectName, editorId);
        return ResponseEntity.ok("Lock released.");
    }

    // Callback endpoint for OnlyOffice
    @PostMapping("/edit/callback")
    public ResponseEntity<String> handleEditCallback(@RequestBody OnlyOfficeCallback callback) {
        // Implement callback handling logic here
        // Example:
        byte[] updatedFileContent = callback.getFileContent();
        minIOService.updateFile(callback.getUserId(), callback.getObjectName(), updatedFileContent);

        // Notify via WebSocket
        Map<String, String> message = new HashMap<>();
        message.put("action", "edit");
        message.put("fileName", callback.getObjectName());
        messagingTemplate.convertAndSend("/topic/files", message);

        return ResponseEntity.ok("File updated successfully.");
    }

}