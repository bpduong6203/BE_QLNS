package DA.backend.constants;

public class LeaveConstants {
    public static final int DEFAULT_ANNUAL_LEAVE_DAYS = 12;
    public static final int DEFAULT_SICK_LEAVE_DAYS = 30;
    public static final int MAX_LEAVE_REQUEST_DAYS = 30;

    public static class ErrorMessages {
        public static final String LEAVE_NOT_FOUND = "Leave request not found";
        public static final String INVALID_DATE_RANGE = "Invalid date range";
        public static final String INSUFFICIENT_LEAVE_DAYS = "Insufficient leave days";
        public static final String OVERLAPPING_LEAVE = "Overlapping leave request exists";
    }
}