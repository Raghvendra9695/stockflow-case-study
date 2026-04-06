import java.util.*;

/**
 * Service to handle Product and Inventory logic for StockFlow.
 */
public class InventoryService {

    // --- Part 1: Code Review & Debugging (Fixed Java Version) ---
    /*
     * ISSUES IDENTIFIED IN ORIGINAL CODE:
     * 1. Transactional Integrity: The original code used two separate commits. 
     * If the second fails, we have a product with no inventory record.
     * 2. Missing Validation: No check for null/empty SKU or negative price.
     * 3. SKU Uniqueness: Did not handle cases where SKU already exists.
     */
    
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // 1. Validation (Professional approach)
        if (request.getSku() == null || request.getSku().isEmpty()) {
            throw new BadRequestException("SKU is mandatory");
        }

        try {
            // 2. Save Product
            Product product = new Product();
            product.setName(request.getName());
            product.setSku(request.getSku());
            product.setPrice(request.getPrice());
            productRepository.save(product);

            // 3. Save Inventory in the same transaction
            Inventory inventory = new Inventory();
            inventory.setProductId(product.getId());
            inventory.setWarehouseId(request.getWarehouseId());
            inventory.setQuantity(request.getInitialQuantity());
            inventoryRepository.save(inventory);

            return new ProductResponse("Product created successfully", product.getId());
        } catch (DataIntegrityViolationException e) {
            // Handle unique SKU constraint
            throw new ConflictException("SKU already exists in the system");
        }
    }

    // --- Part 3: Low Stock Alerts API Implementation ---
    /*
     * Implementation Logic:
     * - Filters inventory by company_id.
     * - Joins Product and Supplier details.
     * - Filters where current quantity <= product's threshold.
     */
    public List<LowStockAlert> getLowStockAlerts(Long companyId) {
        List<Inventory> lowStockItems = inventoryRepository.findLowStockByCompany(companyId);
        List<LowStockAlert> alerts = new ArrayList<>();

        for (Inventory item : lowStockItems) {
            Product product = item.getProduct();
            
            LowStockAlert alert = new LowStockAlert();
            alert.setProductId(product.getId());
            alert.setSku(product.getSku());
            alert.setCurrentStock(item.getQuantity());
            alert.setThreshold(product.getLowStockThreshold());
            
            // Assumption: A helper method calculates this based on recent sales data
            alert.setDaysUntilStockout(calculateEstimatedDaysLeft(product.getId()));
            
            alert.setSupplier(product.getSupplier());
            alerts.add(alert);
        }
        return alerts;
    }

    private int calculateEstimatedDaysLeft(Long productId) {
        // TODO: Integrate with SalesService to get average daily velocity
        // For now, returning a mock value as per case study requirements
        return 10; 
    }
}
