package com.bmstu.lab.calculate.cpi.repository;

import com.bmstu.lab.calculate.cpi.model.entity.CalculateCpi;
import com.bmstu.lab.calculate.cpi.model.enums.CalculateCpiStatus;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculateCpiRepository extends JpaRepository<CalculateCpi, Long> {

  @Modifying
  @Transactional
  @Query("UPDATE CalculateCpi o SET o.status = 'DELETED' WHERE o.id = :calculateCpiId")
  void deleteCalculateCpi(@Param("calculateCpiId") Long calculateCpiId);

  Optional<CalculateCpi> findFirstByStatusAndCreatorId(CalculateCpiStatus status, Long creatorId);

  List<CalculateCpi> findByFormedAtBetweenAndStatus(
      LocalDateTime from, LocalDateTime to, CalculateCpiStatus status);
}
