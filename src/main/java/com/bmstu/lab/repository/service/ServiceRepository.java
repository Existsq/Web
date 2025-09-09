package com.bmstu.lab.repository.service;

import com.bmstu.lab.model.Service;
import java.util.List;

public interface ServiceRepository {

  List<Service> findAll();

  List<Service> findByTitle(String title);

  Service findById(Long id);

  List<Service> findServicesByCart(Long id);
}
