package DA.backend.repository;

import DA.backend.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    boolean existsByDate(LocalDate date);

    @Query("SELECT h FROM Holiday h WHERE YEAR(h.date) = :year OR h.isRecurringYearly = true")
    List<Holiday> findByYear(@Param("year") int year);

    List<Holiday> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
