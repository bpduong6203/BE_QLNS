package DA.backend.enums;

public enum AttendanceStatus {
    PRESENT("Có mặt"),
    LATE("Đi muộn"),
    ABSENT("Vắng mặt"),
    ON_LEAVE("Nghỉ phép");

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}