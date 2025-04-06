package DA.backend.service;

import DA.backend.entity.*;
import DA.backend.repository.DepartmentRepository;
import DA.backend.repository.PositionRepository;
import DA.backend.repository.RoleRepository;
import DA.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private  EmailService emailService;
    public String id;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    DepartmentRepository departmentRepository;
    public static final Random random = new Random();
    private final BCryptPasswordEncoder paswordHash = new BCryptPasswordEncoder();
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean addUser(User user){
//        Optional<Department> optionalDepartment = departmentRepository.findById(id);
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if(optionalUser.isPresent()){
            return false;
        }

        // Gán vai trò mặc định "EMPLOYEE"
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Error: Role EMPLOYEE not found."));

        Set<Role> roles = new HashSet<>();
        roles.add(employeeRole);
        user.setRoles(roles);

         id = IdGenerator.generateUniqueId();
        boolean status = false;
        while (!status){

            String encryptedPassword = paswordHash.encode(id);
            user.setPassword(encryptedPassword);
            user.setId(id);
//            if(optionalDepartment.isPresent()){
//                Department department = optionalDepartment.get();
//                department.getUsers().add(user);
//            }
            try {
                userRepository.save(user);
                status = true;
            }catch (DataIntegrityViolationException ex){
                status = false;
            }
        }
        return true;
    }

    public boolean checkPassword(String password,String encodedPassword){
        return paswordHash.matches(password,encodedPassword);
    }

    public boolean login(String id, String password){
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isPresent()){
            if(!optionalUser.get().isDelete()){
                return checkPassword(password,optionalUser.get().getPassword());
            }
        }
        return false;

    }
    public void deleteUser(String id){
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setDelete(true);
            userRepository.save(user);
        }
    }
    public void updateUser(User user, MultipartFile image) throws IOException {
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if (optionalUser.isPresent()) {
            User user1 = optionalUser.get();

            if (image != null && !image.isEmpty()) {
                String encodedImage = Base64.getEncoder().encodeToString(image.getBytes());
                user1.setImage(encodedImage);
            }

            if (user.getName() != null) {
                user1.setName(user.getName());
            }
            if (user.getAddress() != null) {
                user1.setAddress(user.getAddress());
            }
            if (user.getEmail() != null) {
                user1.setEmail(user.getEmail());
            }
            if (user.getSex() != null) {
                user1.setSex(user.getSex());
            }
            if (user.getBirthDay() != null) {
                user1.setBirthDay(user.getBirthDay());
            }
            if (user.getHomeTown() != null) {
                user1.setHomeTown(user.getHomeTown());
            }
            if (user.getNationality() != null) {
                user1.setNationality(user.getNationality());
            }
            if (user.getPhoneNumber() != null) {
                user1.setPhoneNumber(user.getPhoneNumber());
            }
            if (user.getPosition() != null) {
                user1.setPosition(user.getPosition());
            }
            if (user.getDepartment() != null) {
                user1.setDepartment(user.getDepartment());
            }

            // Thêm xử lý thuộc tính isDelete
            if (user.isDelete() != user1.isDelete()) {
                user1.setDelete(user.isDelete());
            }

            userRepository.save(user1);
        }
    }
    public List<User> listUser(){
        return userRepository.findAll();
    }

    public List<User> listUserDelete(){
        return userRepository.findAll()
                .stream()
                .filter(User::isDelete)
                .collect(Collectors.toList());
    }
    public User checkUser(String id) {
        return userRepository.findById(id).orElse(null);
    }




    public boolean checkSendCode( String sendCode){
        if(sendCode.equals(emailService.Code)){
            return true;
        }
        return false;
    }

    public void resetPassword(String id,String passwordNew, String passwordNew1){
        if(passwordNew.equals(passwordNew1)){
            Optional<User> optionalUser = userRepository.findById(id);
            String encryptedPassword = paswordHash.encode(passwordNew);
            User user = optionalUser.get();
            user.setPassword(encryptedPassword);
            userRepository.save(user);
        }
    }
    public UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setBirthDay(user.getBirthDay());
        userDTO.setNationality(user.getNationality());
        userDTO.setHomeTown(user.getHomeTown());
        userDTO.setAddress(user.getAddress());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setSex(user.getSex());
        userDTO.setImage(user.getImage());
        userDTO.setRole(user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("N/A")); // Chuyển role
        if (user.getPosition() != null) {
            userDTO.setPositionId(user.getPosition().getId());
        }

        return userDTO;

    }
    public Optional<String> getUserImage(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return Optional.ofNullable(user.getImage());
        }
        return Optional.empty();
    }

    public User convertToEntity(UserDTO userDTO) {
        // Lấy thực thể gốc từ cơ sở dữ liệu
        User user = userRepository.findById(userDTO.getId()).orElse(new User());

        if (userDTO.getName() != null) user.setName(userDTO.getName());
        if (userDTO.getBirthDay() != null) user.setBirthDay(userDTO.getBirthDay());
        if (userDTO.getNationality() != null) user.setNationality(userDTO.getNationality());
        if (userDTO.getHomeTown() != null) user.setHomeTown(userDTO.getHomeTown());
        if (userDTO.getAddress() != null) user.setAddress(userDTO.getAddress());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        if (userDTO.getPhoneNumber() != null) user.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getSex() != null) user.setSex(userDTO.getSex());
        if (userDTO.getImage() != null) user.setImage(userDTO.getImage());
        if (userDTO.getPositionId() != null) {
            Position position = positionRepository.findById(userDTO.getPositionId())
                    .orElse(null);
            user.setPosition(position);
        } else {
            user.setPosition(null);
        }

        // Thêm xử lý thuộc tính isDelete
        if (userDTO.getIsDelete() != null) {
            user.setDelete(userDTO.getIsDelete());
        }

        return user;
        // Bảo toàn các trường gốc như password và department
    }
    @Autowired
    private PositionRepository positionRepository;

    public boolean addImages(String id, MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            return false;
        }
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            String encodedImage = Base64.getEncoder().encodeToString(image.getBytes());

            user.setImage(encodedImage);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public boolean updateUserRole(User user, String newRoleName) {
        // Find the new role by name
        Optional<Role> optionalRole = roleRepository.findByName(newRoleName);

        if (optionalRole.isPresent()) {
            Role newRole = optionalRole.get();

            // Update the user's roles
            Set<Role> roles = new HashSet<>();
            roles.add(newRole);
            user.setRoles(roles);

            // Save the updated user
            userRepository.save(user);
            return true;
        } else {
            // Role does not exist
            return false;
        }
    }


    public void updateUserWithoutImage(User user) {
        // Chỉ cập nhật thông tin không bao gồm file ảnh
        userRepository.save(user);
    }


}
