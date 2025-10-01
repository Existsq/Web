package com.bmstu.lab.service;

import com.bmstu.lab.entity.CalculateCpi;
import com.bmstu.lab.entity.CalculateCpiStatus;
import com.bmstu.lab.entity.User;
import com.bmstu.lab.repository.order.OrderRepository;
import com.bmstu.lab.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;

  public CalculateCpi getOrCreateDraftOrder(Long userId) {
    return orderRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElseGet(() -> createDraftOrder(userId));
  }

  private CalculateCpi createDraftOrder(Long userId) {
    User user = userRepository.getReferenceById(userId);
    CalculateCpi newCalculateCpi = new CalculateCpi();
    newCalculateCpi.setStatus(CalculateCpiStatus.DRAFT);
    newCalculateCpi.setCreator(user);
    newCalculateCpi.setPersonalCPI(0.0);
    return orderRepository.save(newCalculateCpi);
  }

  public CalculateCpi getDraftOrder(Long userId) {
    return orderRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .orElse(null);
  }

  public void deleteDraftOrder(Long userId) {
    orderRepository
        .findFirstByStatusAndCreatorId(CalculateCpiStatus.DRAFT, userId)
        .ifPresent(calculateCpi -> orderRepository.deleteOrder(calculateCpi.getId()));
  }
}
