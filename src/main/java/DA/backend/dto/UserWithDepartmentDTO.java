package DA.backend.dto;

import DA.backend.entity.Department;
import lombok.Data;

import java.util.Set;

@Data
public class UserWithDepartmentDTO {
    private String id;
    private String name;
    private Set<Department> departments;
}
