package DA.backend.service;

import DA.backend.entity.Evaluate;
import DA.backend.entity.Role;
import DA.backend.entity.TimeEvaluateRole;
import DA.backend.repository.RoleRepository;
import DA.backend.repository.TimeEvaluateRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class TimeEvaluateRoleService {

    @Autowired
    TimeEvaluateRoleRepository timeEvaluateRoleRepository;
    @Autowired
    RoleRepository roleRepository;
    public List<Role> listRole(){
        return roleRepository.findAll();
    }

    public void addTimeEvaluateRole(List<TimeEvaluateRole> timeEvaluateRoles) {
        timeEvaluateRoleRepository.saveAll(timeEvaluateRoles);

    }

    public void updateTimeEvaluateRole(List<TimeEvaluateRole> timeEvaluateRoles) {
        for (TimeEvaluateRole timeEvaluateRole : timeEvaluateRoles) {
            // Kiểm tra evaluateId và roleId để tìm nếu tồn tại vai trò cho kỳ đánh giá
            if (timeEvaluateRole.getEvaluate() != null && timeEvaluateRole.getEvaluate().getId() != null
                    && timeEvaluateRole.getRole() != null && timeEvaluateRole.getRole().getId() != null) {

                Optional<TimeEvaluateRole> optionalTimeEvaluateRole = timeEvaluateRoleRepository
                        .findByEvaluateIdAndRoleId(timeEvaluateRole.getEvaluate().getId(), timeEvaluateRole.getRole().getId());

                if (optionalTimeEvaluateRole.isPresent()) {
                    // Cập nhật nếu đã tồn tại với evaluateId và roleId
                    TimeEvaluateRole existingTimeEvaluateRole = optionalTimeEvaluateRole.get();
                    existingTimeEvaluateRole.setRole(timeEvaluateRole.getRole());
                    if(timeEvaluateRole.getEndDay() != null){
                        existingTimeEvaluateRole.setEndDay(timeEvaluateRole.getEndDay());
                    }
                   if(timeEvaluateRole.getStartDay() != null){
                       existingTimeEvaluateRole.setStartDay(timeEvaluateRole.getStartDay());
                   }

                    existingTimeEvaluateRole.setEvaluate(timeEvaluateRole.getEvaluate());
                    timeEvaluateRoleRepository.save(existingTimeEvaluateRole);
                } else {
                    // Nếu không có, thêm mới
                    timeEvaluateRoleRepository.save(timeEvaluateRole);
                }
            } else {
                // Nếu không có evaluateId hoặc roleId hợp lệ, cần xử lý thêm mới hoặc lỗi
                System.out.println("Không có evaluateId hoặc roleId hợp lệ.");
            }
        }
    }



    public List<TimeEvaluateRole> list() {
        return timeEvaluateRoleRepository.findAll();
    }
}
