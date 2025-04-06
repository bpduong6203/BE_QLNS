package DA.backend.enums;

public enum ModificationRequestStatus {
    PENDING("Đang chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Từ chối");

    private final String description;

    ModificationRequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}