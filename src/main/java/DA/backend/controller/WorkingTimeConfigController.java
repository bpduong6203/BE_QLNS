package DA.backend.controller;

import DA.backend.entity.Holiday;
import DA.backend.entity.WorkingTimeConfig;
import DA.backend.repository.HolidayRepository;
import DA.backend.repository.WorkingTimeConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config")
@CrossOrigin
public class WorkingTimeConfigController {

    @Autowired
    private WorkingTimeConfigRepository configRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @GetMapping("/working-time")
    public ResponseEntity<WorkingTimeConfig> getCurrentConfig() {
        return ResponseEntity.ok(configRepository.findActive());
    }

    @PostMapping("/working-time")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkingTimeConfig> createConfig(@RequestBody WorkingTimeConfig config) {
        return ResponseEntity.ok(configRepository.save(config));
    }

    @PutMapping("/working-time/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkingTimeConfig> updateConfig(
            @PathVariable Long id,
            @RequestBody WorkingTimeConfig config
    ) {
        if (!configRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        config.setId(id);
        return ResponseEntity.ok(configRepository.save(config));
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<Holiday>> getAllHolidays(
            @RequestParam(required = false) Integer year
    ) {
        if (year != null) {
            return ResponseEntity.ok(holidayRepository.findByYear(year));
        }
        return ResponseEntity.ok(holidayRepository.findAll());
    }

    @PostMapping("/holidays")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Holiday> createHoliday(@RequestBody Holiday holiday) {
        return ResponseEntity.ok(holidayRepository.save(holiday));
    }

    @PutMapping("/holidays/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Holiday> updateHoliday(
            @PathVariable Long id,
            @RequestBody Holiday holiday
    ) {
        if (!holidayRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        holiday.setId(id);
        return ResponseEntity.ok(holidayRepository.save(holiday));
    }

    @DeleteMapping("/holidays/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        if (!holidayRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        holidayRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
