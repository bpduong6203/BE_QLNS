package DA.backend.controller;

import DA.backend.entity.Department;
import DA.backend.entity.User;
import DA.backend.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/department")
@CrossOrigin
public class DepartmentController {
    @Autowired
    DepartmentService departmentService;

    @PostMapping("/add")
    public ResponseEntity<?> addDepartment(@RequestBody @Valid Department department, BindingResult result){
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }
        if(departmentService.addDepartment(department)){
            return ResponseEntity.ok("OK");
        }else
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Failed");

    }
    @GetMapping("/listUsersWithCommonDepartment")
    public ResponseEntity<Set<User>> getUsersWithCommonDepartment(@RequestParam String userId) {
        Set<User> usersWithCommonDepartment = departmentService.listDepartmentUserA(userId); // Gọi service để lấy danh sách người dùng có phòng ban chung
        if (usersWithCommonDepartment == null || usersWithCommonDepartment.isEmpty()) {
            return ResponseEntity.noContent().build(); // Nếu không có người dùng nào, trả về mã 204 No Content
        }
        return ResponseEntity.ok(usersWithCommonDepartment); // Nếu có người dùng, trả về mã 200 OK với dữ liệu
    }
    @PutMapping("/update")
    public ResponseEntity<?> updateDepartment(@RequestBody @Valid Department department, BindingResult result){
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }
        if (departmentService.upadteDepartment(department)){
            return ResponseEntity.ok("OK");
        }else
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Failed");
    }
    @GetMapping("/listUser")
    public Set<User> listUser(@RequestParam Long id){
        return departmentService.listUserDepartment(id);
    }
    @GetMapping("/listDepartment")
    public Set<Department> listDepartment(){
        return departmentService.listDepartment();
    }
    @PostMapping("/addUser")
    public ResponseEntity<?> addUserDepartment(@RequestParam String id, @RequestParam Long department){
        if (departmentService.addUserDepartment(id,department)){
            return ResponseEntity.ok("OK");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Failed");
    }
    @DeleteMapping
    public ResponseEntity<?> deleteDepartment(@RequestParam Long id){
        if(departmentService.deleteDepartment(id)){
             return ResponseEntity.ok("OK");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Failed");
    }

    @GetMapping("/listDepartmentUser")
    public Set<Department> listDepartmentUser(@RequestParam String id){
        return departmentService.listDepartmentUser(id);
    }

}
