package com.bmstu.lab.calculate.cpi.contoller;

import com.bmstu.lab.calculate.cpi.model.dto.CalculateCpiDTO;
import com.bmstu.lab.calculate.cpi.service.CalculateCpiService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculate-cpi")
@AllArgsConstructor
public class CalculateCpiController {

  private final CalculateCpiService calculateCpiService;

  @PostMapping("/{categoryId}/draft")
  public CalculateCpiDTO addCategoryToDraft(
      @PathVariable Long categoryId,
      @RequestParam(required = false, defaultValue = "1") Long userId) {
    return calculateCpiService.addCategoryToDraft(userId, categoryId);
  }
}
