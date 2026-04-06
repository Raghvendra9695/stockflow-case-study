# StockFlow Backend Case Study - Solution

## Part 2: Database Design
I have designed a normalized schema to handle multi-warehouse inventory and product bundling.

### Schema Overview:
- **Companies**: `id, name, created_at`
- **Warehouses**: `id, company_id (FK), name, location`
- **Suppliers**: `id, name, contact_email`
- **Products**: `id, sku (Unique), name, price, low_stock_threshold, supplier_id (FK)`
- **Inventory**: `product_id (FK), warehouse_id (FK), quantity` (Composite Primary Key)
- **Bundles**: `parent_product_id (FK), child_product_id (FK), quantity_needed`

### Missing Requirements / Questions for Product Team:
1. **Threshold Level**: Is the low-stock alert based on a single warehouse or the total company-wide stock? (Current assumption: Warehouse level).
2. **Bundle Logic**: If a 'Bundle' is sold, should the system automatically decrease the stock of individual child products?
3. **Sales Velocity**: For the "days until stockout" calculation, what time window should we consider for average daily sales (e.g., last 30 days)?

### Design Choices:
- Used **Decimal(10,2)** for price to avoid floating-point errors.
- Added a **Unique Index** on SKU for platform-wide consistency.
- **Inventory** table uses a composite key to ensure one entry per product per warehouse.
