package DA.backend.service;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LockService {
    private final Map<String, String> locks = new ConcurrentHashMap<>();

    public boolean acquireLock(String userId, String objectName, String editorId) {
        String key = userId + "/" + objectName;
        return locks.putIfAbsent(key, editorId) == null;
    }

    public void releaseLock(String userId, String objectName, String editorId) {
        String key = userId + "/" + objectName;
        locks.remove(key, editorId);
    }

    public boolean isLocked(String userId, String objectName) {
        String key = userId + "/" + objectName;
        return locks.containsKey(key);
    }

    public String getEditor(String userId, String objectName) {
        String key = userId + "/" + objectName;
        return locks.get(key);
    }
}