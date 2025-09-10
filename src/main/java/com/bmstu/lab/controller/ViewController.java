package com.bmstu.lab.controller;

import com.bmstu.lab.model.Service;
import com.bmstu.lab.repository.service.ServiceRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

  private final ServiceRepository serviceRepository;

  private final String MINIO_BASE_URL;

  public ViewController(ServiceRepository serviceRepository, @Value("${minio.base-url}")String MINIO_BASE_URL) {
    this.serviceRepository = serviceRepository;
    this.MINIO_BASE_URL = MINIO_BASE_URL;
  }

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
    model.addAttribute("cart", serviceRepository.findServicesByCart(1L).size());
    model.addAttribute("baseUrl", MINIO_BASE_URL);

    return "main";
  }

  @GetMapping("/services/{id}")
  public String getServiceById(
      @PathVariable Long id, @RequestParam(required = false) String query, Model model) {
    model.addAttribute("service", serviceRepository.findById(id));
    model.addAttribute("query", query);

    return "details";
  }

  @GetMapping("/cart/{id}")
  public String getCart(
      @PathVariable Long id, @RequestParam(required = false) String query, Model model) {
    model.addAttribute("services", serviceRepository.findServicesByCart(id));
    model.addAttribute("query", query);

    return "order";
  }
}
