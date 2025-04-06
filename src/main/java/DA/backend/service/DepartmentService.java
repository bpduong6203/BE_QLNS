package DA.backend.service;

import DA.backend.entity.Department;
import DA.backend.entity.User;
import DA.backend.repository.DepartmentRepository;
import DA.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DepartmentService {
    @Autowired
    DepartmentRepository departmentRepository;
    @Autowired
    UserRepository userRepository;

    public boolean addDepartment(Department department){

        departmentRepository.save(department);
        return true;
    }

    public  boolean upadteDepartment(Department department){
        Optional<Department>  optionalDepartment = departmentRepository.findById(department.getId());
        Department department1 = optionalDepartment.get();
        if (optionalDepartment.isPresent()){
            department1.setName(department.getName());
            departmentRepository.save(department1);
            return  true;
        }
        return  false;
    }

    public boolean  deleteDepartment(Long id){
        Optional<Department> optionalDepartment = departmentRepository.findById(id);
        if (optionalDepartment.isPresent()){
            departmentRepository.deleteById(id);
            return  true;
        }
        return false;
    }

    public Set<Department> listDepartment(){
       return new HashSet<>(departmentRepository.findAll());
    }

    public boolean addUserDepartment(String id, Long department){
        Optional<User> optionalUser = userRepository.findById(id);
        Optional<Department> optionalDepartment = departmentRepository.findById(department);
        if(optionalUser.isPresent() && optionalDepartment.isPresent()){
            User user1 = optionalUser.get();
            Department department1 = optionalDepartment.get();
//            user1.getDepartment().add(department1);
            department1.getUsers().add(user1);
//            departmentRepository.save(department1);
            userRepository.save(user1);
            return  true;
        }
        return  false;
    }
    public Set<User> listUserDepartment(Long id){
        Optional<Department> optionalDepartment = departmentRepository.findById(id);
        if(optionalDepartment.isPresent()){
            Department department = optionalDepartment.get();
            return  new HashSet<>(userRepository.findAll()
                    .stream()
                    .filter( t -> t.getDepartment().contains(department) )
                    .collect(Collectors.toList())
            );
        }
       return null;
    }
    public boolean deleteUserDepartment(String id,Long departmentId){
        Optional<User> optionalUser = userRepository.findById(id);
        Optional<Department> optionalDepartment = departmentRepository.findById(departmentId);
        if (optionalUser.isPresent() && optionalDepartment.isPresent()){
            User user = optionalUser.get();
            Department department = optionalDepartment.get();
            user.getDepartment().remove(department);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    public Set<Department> listDepartmentUser(String id){
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            return  new HashSet<>(departmentRepository.findAll()
                    .stream()
                    .filter(t -> t.getUsers().contains(user))
                    .collect(Collectors.toList())
            );
        }
        return null;
    }
    public Set<User> listDepartmentUserA(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId); // Lấy thông tin người dùng hiện tại
        if (optionalUser.isPresent()) {
            User currentUser = optionalUser.get();
            Set<Department> currentDepartments = currentUser.getDepartment(); // Lấy Set<Department> của người dùng hiện tại

            // Lọc danh sách người dùng có ít nhất một phòng ban chung
            return userRepository.findAll()
                    .stream()
                    .filter(user -> user.getDepartment() != null && !user.getDepartment().isEmpty() &&
                            user.getDepartment().stream().anyMatch(department -> currentDepartments.contains(department))) // Kiểm tra xem người dùng có ít nhất 1 phòng ban chung với người dùng hiện tại
                    .collect(Collectors.toSet()); // Trả về Set các user có ít nhất một phòng ban chung
        }
        return null; // Trường hợp người dùng không tồn tại
    }



}
