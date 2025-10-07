package com.bmstu.lab.calculate.cpi.model.entity;

import com.bmstu.lab.calculate.cpi.category.model.entity.CalculateCpiCategory;
import com.bmstu.lab.calculate.cpi.model.enums.CalculateCpiStatus;
import com.bmstu.lab.user.model.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "calculate_cpi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculateCpi {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CalculateCpiStatus status = CalculateCpiStatus.DRAFT;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss",
      timezone = "Europe/Moscow")
  private LocalDateTime createdAt;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss",
      timezone = "Europe/Moscow")
  private LocalDateTime formedAt;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss",
      timezone = "Europe/Moscow")
  private LocalDateTime completedAt;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate comparisonDate;

  @ManyToOne
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @ManyToOne
  @JoinColumn(name = "moderator_id")
  private User moderator;

  @Column(name = "personal_cpi")
  private Double personalCPI;

  @Column private int positions = 0;

  @OneToMany(
      mappedBy = "calculateCpi",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<CalculateCpiCategory> calculateCpiCategories = new ArrayList<>();
}
