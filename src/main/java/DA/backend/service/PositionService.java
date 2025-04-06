package DA.backend.service;

import DA.backend.entity.Position;
import DA.backend.repository.PositionRepository;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class PositionService {
    @Autowired
    PositionRepository positionRepository;

    public boolean add(Position position){
        positionRepository.save(position);
        return true;
    }
    public boolean update(Position position){
        Optional<Position> optionalPosition = positionRepository.findById(position.getId());
        if(optionalPosition.isPresent()){
            Position position1 = optionalPosition.get();
            position1.setName(position.getName());
            positionRepository.save(position1);
            return true;
        }
       return false;
    }
    public boolean delete(Long id){
        Optional<Position> optionalPosition = positionRepository.findById(id);
        if(optionalPosition.isPresent()){
            positionRepository.deleteById(id);
            return true;
        }
        return false;
    }
    public Set<Position> list(){
       return new HashSet<>(positionRepository.findAll());
    }

}
