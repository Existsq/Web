package com.bmstu.lab.service;

import com.bmstu.lab.dto.CartSummaryDTO;
import com.bmstu.lab.entity.CalculateCpi;
import com.bmstu.lab.entity.CalculateCpiCategory;
import com.bmstu.lab.entity.Category;
import com.bmstu.lab.repository.calculatecpi.CalculateCpiCategoryRepository;
import com.bmstu.lab.repository.calculatecpi.CalculateCpiRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CalculateCpiService calculateCpiService;
    private final CalculateCpiCategoryRepository categoryRepository;
    private final CalculateCpiRepository calculateCpiRepository;
    private final CpiCalculator cpiCalculator;

    public void addCategoryToCalculateCpi(Long userId, Long categoryId, Category category) {
        CalculateCpi calculateCpi = calculateCpiService.getOrCreateDraftCalculateCpi(userId);

        boolean exists = categoryRepository.findByCalculateCpi(calculateCpi).stream()
                .anyMatch(oc -> oc.getCategory().getId().equals(categoryId));

        if (!exists) {
            CalculateCpiCategory calculateCpiCategory = new CalculateCpiCategory();
            calculateCpiCategory.setCalculateCpi(calculateCpi);
            calculateCpiCategory.setCategory(category);
            calculateCpiCategory.setUserSpent((double) category.getBasePrice());
            categoryRepository.save(calculateCpiCategory);
        }
    }

    public CartSummaryDTO calculateCartSummary(Long userId) {
        CalculateCpi calculateCpi = calculateCpiService.getDraftCalculateCpi(userId);
        if (calculateCpi == null) {
            return new CartSummaryDTO(0.0, List.of());
        }

        List<CalculateCpiCategory> calculateCpiCategories = categoryRepository.findByCalculateCpi(calculateCpi);

        double totalSpent = cpiCalculator.calculateTotalSpent(calculateCpiCategories);
        double personalCPI = cpiCalculator.calculatePersonalCPI(calculateCpiCategories);

        calculateCpi.setPersonalCPI(personalCPI);
        calculateCpiRepository.save(calculateCpi);

        List<Category> categories = cpiCalculator.mapToCategoriesWithCoefficient(calculateCpiCategories, totalSpent);

        return new CartSummaryDTO(personalCPI, categories);
    }
}