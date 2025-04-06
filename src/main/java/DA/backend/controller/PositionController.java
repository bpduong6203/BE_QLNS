package DA.backend.controller;

import DA.backend.entity.Position;
import DA.backend.service.PositionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("api/position")
@CrossOrigin
public class PositionController {
    @Autowired
    PositionService positionService;

    @PostMapping("/add")
    public ResponseEntity<?> addPosition(@RequestBody Position position){
        if (positionService.add(position))
            return ResponseEntity.ok("OK");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Failed");
    }
    @GetMapping("/list")
    public Set<Position> listPosition(){
        return positionService.list();
    }
    @PutMapping("/update")
    public ResponseEntity<?> updatePosition(@RequestBody Position position){
        if(positionService.update(position))
            return ResponseEntity.ok("OK");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Failed");
    }
    @DeleteMapping
    public ResponseEntity<?> deletePosition(@RequestParam Long id){
        if(positionService.delete(id))
            return ResponseEntity.ok("OK");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Failed");
    }
}
