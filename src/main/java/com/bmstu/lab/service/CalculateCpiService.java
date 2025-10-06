package com.bmstu.lab.service;

import com.bmstu.lab.entity.CalculateCpi;
import com.bmstu.lab.entity.User;
import com.bmstu.lab.entity.enums.CalculateCpiStatus;
import com.bmstu.lab.repository.calculatecpi.CalculateCpiRepository;
import com.bmstu.lab.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateCpiService {

  private final CalculateCpiRepository calculateCpiRepository;
  private final UserRepository userRepository;

  public CalculateCpi getOrCreateDraftCalculateCpi(Long userId) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElseGet(() -> createDraftCalculateCpi(userId));
  }

  private CalculateCpi createDraftCalculateCpi(Long userId) {
    User user = userRepository.getReferenceById(userId);
    CalculateCpi newCalculateCpi = new CalculateCpi();
    newCalculateCpi.setStatus(CalculateCpiStatus.DRAFT);
    newCalculateCpi.setCreator(user);
    newCalculateCpi.setPersonalCPI(0.0);
    return calculateCpiRepository.save(newCalculateCpi);
  }

  public CalculateCpi getDraftCalculateCpi(Long userId) {
    return calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElse(null);
  }

  public void deleteDraftCalculateCpi(Long userId) {
    calculateCpiRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .ifPresent(calculateCpi -> calculateCpiRepository.deleteCalculateCpi(calculateCpi.getId()));
  }
}
