package DA.backend.enums;

public enum LeaveStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Từ chối"),
    REVOKED("Bị hủy"), //khi đã duyệt
    CANCELLED("Đã hủy");

    private final String description;

    LeaveStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}