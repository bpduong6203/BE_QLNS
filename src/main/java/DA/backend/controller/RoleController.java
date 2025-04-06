package DA.backend.controller;

import DA.backend.entity.Role;
import DA.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
@CrossOrigin
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    // Endpoint to list all roles
    @GetMapping("/list")
    public List<Role> listRoles() {
        return roleRepository.findAll();
    }
}
