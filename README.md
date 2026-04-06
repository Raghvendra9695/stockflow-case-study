# StockFlow - Backend Case Study Solution
**Candidate:** Raghvendra Yadav  
**Total Time Taken:** ~90 Minutes

## Part 2: Database Design
### Tables & Relationships
- **Companies**: `id (PK), name, industry`
- **Warehouses**: `id (PK), company_id (FK), name, address`
- **Products**: `id (PK), sku (Unique), name, price (Decimal), low_stock_threshold (Int)`
- **Inventory**: `product_id (FK), warehouse_id (FK), quantity (Int)`
- **Suppliers**: `id (PK), name, contact_email`
- **Product_Suppliers**: `product_id (FK), supplier_id (FK)` (Many-to-Many)
- **Bundles**: `parent_product_id (FK), child_product_id (FK), quantity`

### Missing Requirements / Questions
1. Does the low-stock threshold apply per warehouse or across the entire company?
2. For bundles, should the system automatically recalculate stock based on child products?
3. Are there different price tiers for B2B customers?

### Design Decisions
- Used a **Composite Primary Key** on the Inventory table (product_id + warehouse_id) to prevent duplicate entries.
- Added a **Unique Constraint** on the `sku` column in the Products table.
