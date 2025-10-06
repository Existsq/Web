package com.bmstu.lab.repository.calculatecpi;

import com.bmstu.lab.entity.CalculateCpi;
import com.bmstu.lab.entity.enums.CalculateCpiStatus;
import jakarta.transaction.Transactional;
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
}
