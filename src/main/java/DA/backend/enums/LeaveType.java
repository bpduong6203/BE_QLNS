package DA.backend.enums;

public enum LeaveType {
    SICK_LEAVE("Nghỉ ốm"),
    ANNUAL_LEAVE("Nghỉ phép năm"),
    UNPAID_LEAVE("Nghỉ không lương"),
    MATERNITY_LEAVE("Nghỉ thai sản"),
    OTHER("Khác");

    private final String description;

    LeaveType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
