package com.bmstu.lab.presentation.controller;

import com.bmstu.lab.application.dto.CalculateCpiDTO;
import com.bmstu.lab.application.service.CalculateCpiService;
import com.bmstu.lab.infrastructure.persistence.enums.CalculateCpiStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculate-cpi")
@AllArgsConstructor
public class CalculateCpiController {

  private final CalculateCpiService calculateCpiService;

  @GetMapping("/draft-info")
  public CalculateCpiService.DraftInfoDTO getDraftInfo(
      @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.getDraftInfoByUsername(userDetails.getUsername());
  }

  @GetMapping
  public List<CalculateCpiDTO> getAll(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @RequestParam(required = false) CalculateCpiStatus status,
      @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.findAllFiltered(from, to, status, userDetails.getUsername());
  }

  @GetMapping("/{id}")
  public CalculateCpiDTO getById(
      @PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.getById(id, userDetails.getUsername());
  }

  @PutMapping("/{id}")
  public CalculateCpiDTO update(
      @PathVariable Long id,
      @RequestBody CalculateCpiDTO dto,
      @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.update(id, dto, userDetails.getUsername());
  }

  // Сформировать создателем
  @PutMapping("/form/{draftId}")
  public CalculateCpiDTO formDraft(
      @PathVariable Long draftId, @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.formDraft(userDetails.getUsername(), draftId);
  }

  // Завершить/отклонить модератором
  @PutMapping("/deny-complete/{id}")
  @PreAuthorize("hasAuthority('ROLE_MODERATOR')")
  public CalculateCpiDTO denyOrComplete(
      @PathVariable Long id,
      @RequestParam boolean approve,
      @AuthenticationPrincipal UserDetails userDetails) {
    return calculateCpiService.denyOrComplete(id, userDetails.getUsername(), approve);
  }

  @DeleteMapping("/{draftId}")
  public void delete(@PathVariable Long draftId, @AuthenticationPrincipal UserDetails userDetails) {
    calculateCpiService.delete(draftId, userDetails.getUsername());
  }
}
