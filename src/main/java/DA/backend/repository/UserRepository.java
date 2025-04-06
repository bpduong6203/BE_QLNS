package DA.backend.repository;

import DA.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, String> {

   Optional<User> findByEmail(String email);

   @Query("SELECT u FROM User u JOIN FETCH u.department WHERE u.id = :userId")
   Optional<User> findByIdWithDepartments(@Param("userId") String userId);
}
