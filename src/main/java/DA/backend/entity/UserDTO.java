package DA.backend.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserDTO {
    private String id;
    private String name;
    private Date birthDay;
    private String nationality;
    private String homeTown;
    private String address;
    private String email;
    private String phoneNumber;
    private String sex;
    private String image;
    private String role;
    private Long positionId;
    private Boolean isDelete;

    // Các getter và setter đã được tự động tạo bởi @Getter và @Setter
}
