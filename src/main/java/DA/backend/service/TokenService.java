package DA.backend.service;



import DA.backend.entity.FileShareMetadata;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenService{
    private Map<String, FileShareMetadata> tokenStore = new HashMap<>();

    public String createToken(FileShareMetadata metadata) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, metadata);
        return token;
    }

    public FileShareMetadata getMetadataFromToken(String token) {
        return tokenStore.get(token);
    }

    @Scheduled(fixedRate = 3600000) // Mỗi giờ kiểm tra và xóa token hết hạn
    public void cleanupExpiredTokens() {
        Date now = new Date();
        tokenStore.entrySet().removeIf(entry -> entry.getValue().getExpirationDate().before(now));
    }
}