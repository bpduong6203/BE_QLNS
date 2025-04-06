package DA.backend.controller;

import DA.backend.entity.Role;
import DA.backend.entity.TimeEvaluateRole;
import DA.backend.service.TimeEvaluateRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeEvaluateRole")
@CrossOrigin
public class TimeEvaluateRoleController {

    @Autowired
    private TimeEvaluateRoleService timeEvaluateRoleService;

    @GetMapping("/listRole")
    public ResponseEntity<List<Role>> listRole() {
        return ResponseEntity.ok(timeEvaluateRoleService.listRole());
    }


    @PostMapping("/add")
    public ResponseEntity<Void> addTimeEvaluateRole(@RequestBody List<TimeEvaluateRole> timeEvaluateRoles) {
        timeEvaluateRoleService.addTimeEvaluateRole(timeEvaluateRoles);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateTimeEvaluateRole(@RequestBody List<TimeEvaluateRole> timeEvaluateRole) {
        timeEvaluateRoleService.updateTimeEvaluateRole(timeEvaluateRole);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<TimeEvaluateRole>> list() {
        return ResponseEntity.ok(timeEvaluateRoleService.list());
    }
}
