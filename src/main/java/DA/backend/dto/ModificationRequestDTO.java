package DA.backend.dto;
import lombok.Data;

@Data
public class ModificationRequestDTO {
    private String approverId;
    private boolean approved;
    private String comment;

    // Getter/setter methods
    public String getApproverId() { return approverId; }
    public void setApproverId(String approverId) { this.approverId = approverId; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
