package com.bmstu.lab.presentation.controller;

import com.bmstu.lab.application.dto.CalculateCpiDTO;
import com.bmstu.lab.infrastructure.persistence.enums.CalculateCpiStatus;
import com.bmstu.lab.application.service.CalculateCpiService;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
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
  public CalculateCpiService.DraftInfoDTO getDraftInfo() {
    Long userId = 1L;
    return calculateCpiService.getDraftInfo(userId);
  }

  @GetMapping
  public List<CalculateCpiDTO> getAll(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @RequestParam(required = false) CalculateCpiStatus status) {

    return calculateCpiService.findAllFiltered(from, to, status);
  }

  @GetMapping("/{id}")
  public CalculateCpiDTO getById(@PathVariable Long id) {
    return calculateCpiService.getById(id);
  }

  @PutMapping("/{id}")
  public CalculateCpiDTO update(@PathVariable Long id, @RequestBody CalculateCpiDTO dto) {
    return calculateCpiService.update(id, dto);
  }

  @PutMapping("/form/{draftId}")
  public CalculateCpiDTO formDraft(@PathVariable Long draftId) {
    Long userId = 1L;
    return calculateCpiService.formDraft(userId, draftId);
  }

  @PutMapping("/deny/{id}")
  public CalculateCpiDTO deny(@PathVariable Long id, @RequestParam boolean approve) {
    return calculateCpiService.denyOrComplete(id, 1L, approve);
  }

  @DeleteMapping("/{draftId}")
  public void delete(@PathVariable Long draftId) {
    calculateCpiService.delete(draftId, 1L);
  }
}
