package com.bmstu.lab.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsyncResultDTO {
  private Double personalCPI;
  private Boolean success;
}

