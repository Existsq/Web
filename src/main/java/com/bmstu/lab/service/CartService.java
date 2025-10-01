package com.bmstu.lab.service;

import com.bmstu.lab.dto.CartSummaryDTO;
import com.bmstu.lab.entity.CalculateCpi;
import com.bmstu.lab.entity.CalculateCpiCategory;
import com.bmstu.lab.entity.Category;
import com.bmstu.lab.repository.order.CalculateCpiCategoryRepository;
import com.bmstu.lab.repository.order.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final OrderService orderService;
    private final CalculateCpiCategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final CpiCalculator cpiCalculator;

    public void addCategoryToOrder(Long userId, Long categoryId, Category category) {
        CalculateCpi calculateCpi = orderService.getOrCreateDraftOrder(userId);

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
        CalculateCpi calculateCpi = orderService.getDraftOrder(userId);
        if (calculateCpi == null) {
            return new CartSummaryDTO(0.0, List.of());
        }

        List<CalculateCpiCategory> orderCategories = categoryRepository.findByCalculateCpi(calculateCpi);

        double totalSpent = cpiCalculator.calculateTotalSpent(orderCategories);
        double personalCPI = cpiCalculator.calculatePersonalCPI(orderCategories);

        calculateCpi.setPersonalCPI(personalCPI);
        orderRepository.save(calculateCpi);

        List<Category> categories = cpiCalculator.mapToCategoriesWithCoefficient(orderCategories, totalSpent);

        return new CartSummaryDTO(personalCPI, categories);
    }
}