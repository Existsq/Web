package com.bmstu.lab.entity;

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

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "formed_at")
  private LocalDateTime formedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "comparison_date")
  private LocalDate comparisonDate;

  @ManyToOne
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @ManyToOne
  @JoinColumn(name = "moderator_id")
  private User moderator;

  @Column(name = "personal_cpi")
  private Double personalCPI;

  @OneToMany(
      mappedBy = "calculateCpi",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  private List<CalculateCpiCategory> calculateCpiCategories = new ArrayList<>();

  private Double calculatePersonalCPI() {
    return 13.2;
  }
}
