import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class InventoryService {

    /**
     * Part 1: Debugged & Corrected Product Creation
     * Ensuring Atomicity and Validation
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // 1. Mandatory Field Validation
        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new IllegalArgumentException("SKU is required.");
        }

        try {
            // 2. Create Product Entry
            Product product = new Product();
            product.setName(request.getName());
            product.setSku(request.getSku());
            product.setPrice(request.getPrice());
            productRepository.save(product);

            // 3. Initialize Inventory (Flushing within same transaction)
            Inventory inventory = new Inventory();
            inventory.setProductId(product.getId());
            inventory.setWarehouseId(request.getWarehouseId());
            inventory.setQuantity(request.getInitialQuantity());
            inventoryRepository.save(inventory);

            return new ProductResponse("SUCCESS", product.getId());
        } catch (DataIntegrityViolationException ex) {
            // Handles Unique SKU constraint violation
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists.");
        }
    }

    /**
     * Part 3: Low-Stock Alerts Implementation
     */
    public List<LowStockAlert> getLowStockAlerts(Long companyId) {
        // Fetching data using a custom Repository Query that filters by company_id
        // and checks quantity <= low_stock_threshold
        List<Object[]> results = inventoryRepository.findLowStockAlertsByCompany(companyId);
        
        List<LowStockAlert> alerts = new ArrayList<>();
        for (Object[] row : results) {
            LowStockAlert alert = new LowStockAlert();
            alert.setProductId((Long) row[0]);
            alert.setProductName((String) row[1]);
            alert.setSku((String) row[2]);
            alert.setWarehouseId((Long) row[3]);
            alert.setWarehouseName((String) row[4]);
            alert.setCurrentStock((Integer) row[5]);
            alert.setThreshold((Integer) row[6]);
            
            // Business Logic Assumption: Days until stockout is calculated 
            // by (Current Stock / Avg Daily Sales Velocity)
            alert.setDaysUntilStockout(calculateVelocity((Long) row[0]));

            // Mapping Supplier Object
            SupplierDTO supplier = new SupplierDTO((Long) row[7], (String) row[8], (String) row[9]);
            alert.setSupplier(supplier);
            
            alerts.add(alert);
        }
        return alerts;
    }

    private int calculateVelocity(Long productId) {
        // TODO: Integrate with Sales Analytics Service
        return 12; // Mock assumption for case study
    }
}
