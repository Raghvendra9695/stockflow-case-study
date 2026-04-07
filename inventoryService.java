
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public InventoryService(ProductRepository productRepository,
                            InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Part 1: Debugged & Corrected Product Creation
     * - Ensures atomic transaction
     * - Adds proper validation
     * - Handles SKU uniqueness
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        // ---- Validation ----
        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new IllegalArgumentException("SKU is required.");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required.");
        }

        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive.");
        }

        try {
            // ---- Create Product ----
            Product product = new Product();
            product.setName(request.getName().trim());
            product.setSku(request.getSku().trim());
            product.setPrice(request.getPrice());

            productRepository.save(product);

            // ---- Optional Inventory (multi-warehouse support) ----
            if (request.getWarehouseId() != null) {
                Inventory inventory = new Inventory();
                inventory.setProductId(product.getId());
                inventory.setWarehouseId(request.getWarehouseId());
                inventory.setQuantity(
                    request.getInitialQuantity() != null ? request.getInitialQuantity() : 0
                );

                inventoryRepository.save(inventory);
            }

            return new ProductResponse("Product created successfully", product.getId());

        } catch (DataIntegrityViolationException ex) {
            // Handles duplicate SKU or DB constraint violations
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "SKU already exists or constraint violation"
            );
        }
    }

    /**
     * Part 3: Low-Stock Alerts Implementation
     * - Handles multiple warehouses
     * - Filters by low stock threshold
     * - Filters by recent sales
     * - Includes supplier details
     */
    public LowStockResponse getLowStockAlerts(Long companyId) {

        // Custom query expected to return joined data:
        // product, warehouse, supplier info
        List<Object[]> results =
                inventoryRepository.findLowStockAlertsByCompany(companyId);

        List<LowStockAlert> alerts = new ArrayList<>();

        for (Object[] row : results) {

            Long productId = (Long) row[0];

            // ---- Rule: Only include products with recent sales ----
            if (!hasRecentSales(productId)) {
                continue;
            }

            LowStockAlert alert = new LowStockAlert();
            alert.setProductId(productId);
            alert.setProductName((String) row[1]);
            alert.setSku((String) row[2]);

            alert.setWarehouseId((Long) row[3]);
            alert.setWarehouseName((String) row[4]);

            alert.setCurrentStock((Integer) row[5]);
            alert.setThreshold((Integer) row[6]);

            // ---- Calculate stockout days ----
            alert.setDaysUntilStockout(
                    calculateDaysUntilStockout(productId, (Integer) row[5])
            );

            // ---- Supplier mapping ----
            SupplierDTO supplier = new SupplierDTO(
                    (Long) row[7],
                    (String) row[8],
                    (String) row[9]
            );
            alert.setSupplier(supplier);

            alerts.add(alert);
        }

        return new LowStockResponse(alerts, alerts.size());
    }

    /**
     * Assumption:
     * Checks if product had sales in last 30 days
     */
    private boolean hasRecentSales(Long productId) {
        // TODO: integrate with SalesService / order table
        return true;
    }

    /**
     * Assumption:
     * Calculates days until stockout using avg daily sales
     */
    private int calculateDaysUntilStockout(Long productId, int currentStock) {
        int avgDailySales = 2; // mock value for case study

        if (avgDailySales <= 0) {
            return Integer.MAX_VALUE;
        }

        return currentStock / avgDailySales;
    }
}
```
