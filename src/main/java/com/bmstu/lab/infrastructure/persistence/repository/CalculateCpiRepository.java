package com.bmstu.lab.infrastructure.persistence.repository;

import com.bmstu.lab.infrastructure.persistence.entity.CalculateCpi;
import com.bmstu.lab.infrastructure.persistence.enums.CalculateCpiStatus;
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
  @Query(
      "UPDATE CalculateCpi o SET o.status = com.bmstu.lab.infrastructure.persistence.enums.CalculateCpiStatus.DELETED WHERE o.id = :calculateCpiId")
  void deleteCalculateCpi(@Param("calculateCpiId") Long calculateCpiId);

  Optional<CalculateCpi> findFirstByStatusAndCreatorUsername(
      CalculateCpiStatus status, String creatorUsername);

  List<CalculateCpi> findByFormedAtBetweenAndStatus(
      LocalDateTime from, LocalDateTime to, CalculateCpiStatus status);

  List<CalculateCpi> findByFormedAtBetween(LocalDateTime from, LocalDateTime to);

  List<CalculateCpi> findByStatus(CalculateCpiStatus status);

  Optional<CalculateCpi> findFirstByCreatorId(Long creatorId);
}
