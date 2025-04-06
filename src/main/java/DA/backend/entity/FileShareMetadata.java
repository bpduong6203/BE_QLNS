package DA.backend.entity;

import java.util.Date;

public class FileShareMetadata {
    private String userId;
    private String objectName;
    private String permission;
    private Date expirationDate;

    public FileShareMetadata(String userId, String objectName, String permission, Date expirationDate) {
        this.userId = userId;
        this.objectName = objectName;
        this.permission = permission;
        this.expirationDate = expirationDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean hasAccess() {
        return permission.equals("read") || permission.equals("edit");
    }
}