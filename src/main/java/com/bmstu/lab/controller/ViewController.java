package com.bmstu.lab.controller;

import com.bmstu.lab.model.Service;
import com.bmstu.lab.repository.service.ServiceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ViewController {

  private final ServiceRepository serviceRepository;

  @GetMapping("/services")
  public String servicesPage(@RequestParam(required = false) String query, Model model) {
    List<Service> services;

    if (query != null && !query.isBlank()) {
      services = serviceRepository.findByTitle(query);
    } else {
      services = serviceRepository.findAll();
    }

    model.addAttribute("services", services);
    model.addAttribute("query", query);

    return "main";
  }

  @GetMapping("/services/{id}")
  public String getServiceById(
      @PathVariable Long id, @RequestParam(required = false) String query, Model model) {
    model.addAttribute("service", serviceRepository.findById(id));
    model.addAttribute("query", query);

    return "details";
  }
}
