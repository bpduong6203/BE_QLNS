package DA.backend.enums;

public enum AttendanceCodeType {
    CHECK_IN("Điểm danh vào"),
    CHECK_OUT("Điểm danh ra");

    private final String description;

    AttendanceCodeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}