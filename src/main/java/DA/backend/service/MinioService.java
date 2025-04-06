package DA.backend.service;


import DA.backend.controller.FileMetadataDTO;
import DA.backend.entity.FileShareMetadata;
import io.minio.*;
import io.minio.messages.Item;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.html.AbstractHtmlExporter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MinioService {
    private final MinioClient minioClient;
    private final TokenService tokenService;

    @Autowired
    public MinioService(
            @Value("${minio.url}") String url,
            @Value("${minio.accessKey}") String accessKey,
            @Value("${minio.secretKey}") String secretKey,
            TokenService tokenService) {
        this.minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
        this.tokenService = tokenService;
    }

    public String uploadFile(MultipartFile file, String userId) {
        String bucketName = "bucket-" + userId;
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            String objectName = userId + "_" + file.getOriginalFilename().replaceAll("[^\\x20-\\x7E]", "_");
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return "File uploaded successfully: " + objectName;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to upload file: " + e.getMessage();
        }
    }

    public List<FileMetadataDTO> listFilesFromMinIO(String userId) {
        String bucketName = "bucket-" + userId;
        List<FileMetadataDTO> fileList = new ArrayList<>();
        try {
            Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
            for (Result<Item> result : objects) {
                Item item = result.get();
                FileMetadataDTO fileMetadataDTO = new FileMetadataDTO();
                fileMetadataDTO.setFileName(item.objectName());
                fileMetadataDTO.setFileSize(item.size());
                fileMetadataDTO.setUploadDate(dateFormat.format(Date.from(item.lastModified().toInstant())));
                fileMetadataDTO.setFileType("application/octet-stream");
                fileList.add(fileMetadataDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public String createShareableLink(String userId, String fileName, String permission, Date expirationDate) {
        FileShareMetadata metadata = new FileShareMetadata(userId, fileName, permission, expirationDate);
        return tokenService.createToken(metadata);
    }

    public byte[] accessSharedFile(String token) {
        try {
            FileShareMetadata metadata = tokenService.getMetadataFromToken(token);
            if (metadata == null || metadata.getExpirationDate().before(new Date())) {
                throw new SecurityException("Token không hợp lệ hoặc đã hết hạn.");
            }
            String bucketName = "bucket-" + metadata.getUserId();
            String objectName = metadata.getObjectName();
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return stream.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] downloadFile(String userId, String objectName) {
        String bucketName = "bucket-" + userId;
        String sanitizedObjectName = objectName.replaceAll("[^\\x20-\\x7E]", "_");
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(sanitizedObjectName)
                    .build());
            return stream.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String deleteFile(String userId, String objectName) {
        String bucketName = "bucket-" + userId;
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return "File deleted successfully: " + objectName;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to delete file: " + e.getMessage();
        }
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public String convertDocxToHtml(String userId, String objectName) {
        String bucketName = "bucket-" + userId;
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build())) {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(stream);
            AbstractHtmlExporter.HtmlSettings htmlSettings = new AbstractHtmlExporter.HtmlSettings();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Docx4J.toHTML(htmlSettings, out, Docx4J.FLAG_EXPORT_PREFER_XSL);
            return out.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert .docx to PDF
     */
    public byte[] convertDocxToPdf(String userId, String objectName) {
        String bucketName = "bucket-" + userId;
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build())) {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(stream);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Docx4J.toPDF(wordMLPackage, out);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate Edit URL for OnlyOffice
     */
    public String generateEditUrl(String userId, String objectName) {
        // Implement OnlyOffice URL generation logic here
        // This is a placeholder implementation
        String token = createShareableLink(userId, objectName, "edit", new Date(System.currentTimeMillis() + 3600000));
        return "https://your-onlyoffice-server.com/edit?token=" + token;
    }

    /**
     * Update existing file with new content
     */
    public void updateFile(String userId, String objectName, byte[] content) {
        String bucketName = "bucket-" + userId;
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}