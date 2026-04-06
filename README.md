# StockFlow - B2B Inventory Management System
**Backend Case Study Submission**
**Candidate:** Raghvendra Yadav

---

## 🔍 Part 1: Code Review & Debugging
The original Python/Flask API provided for product creation had several critical flaws that would cause data inconsistency in a production environment.

### Identified Issues & Impact:
1. **Non-Atomic Transactions (Critical):**
   - **Problem:** The code uses two separate `db.session.commit()` calls.
   - **Impact:** If the first commit succeeds but the second fails (e.g., database timeout or invalid `initial_quantity`), the product is created without an inventory record. This leads to "ghost products" that exist in the system but cannot be tracked or sold.
2. **Missing Input Validation:**
   - **Problem:** No checks for mandatory fields (`sku`, `name`, `price`) or data types.
   - **Impact:** Sending a null value or missing a key in the JSON request would cause the application to crash with a `KeyError` (500 Internal Server Error).
3. **SKU Uniqueness Violation:**
   - **Problem:** The platform requires unique SKUs, but the code doesn't verify if the SKU already exists before attempting to save.
   - **Impact:** Duplicate SKUs would lead to severe operational errors in tracking and logistics.

**✅ Fix Provided:** In my `InventoryService.java` file, I have implemented a single `@Transactional` block to ensure atomicity and added proper exception handling for unique constraints.

---

## 🗄 Part 2: Database Design
Designed a normalized relational schema to handle multi-warehouse inventory and product bundling.

### Schema Structure:
- **Companies**: `id (PK), name, plan_type`
- **Warehouses**: `id (PK), company_id (FK), name, address`
- **Products**: `id (PK), sku (Unique), name, price (Decimal), low_stock_threshold (Int)`
- **Inventory**: `product_id (FK), warehouse_id (FK), quantity (Int)` *(Composite PK: product_id + warehouse_id)*
- **Suppliers**: `id (PK), name, contact_email`
- **Bundles**: `parent_product_id (FK), child_product_id (FK), quantity_required`

### Missing Requirements & Questions:
1. **Alert Logic:** Is the low-stock threshold global for the company or specific to each warehouse? (My design assumes per-product global threshold).
2. **Bundle Fulfillment:** Does selling a bundle automatically trigger stock deduction for all child products in real-time?
3. **Sales History:** What is the definition of "recent sales activity" for the alert system (e.g., last 7 days or 30 days)?

---

## 🚀 Part 3: API Implementation
**Endpoint:** `GET /api/companies/{company_id}/alerts/low-stock`

### Implementation Approach:
- **Efficiency:** Used a Join-based SQL approach to fetch Product, Inventory, and Supplier data in a single database trip to avoid N+1 query issues.
- **Filtering:** Logic only includes products where `current_stock <= threshold` AND there is recent sales activity.
- **Scalability:** The design handles companies with hundreds of warehouses by filtering at the database level rather than in-memory.

### Assumptions:
- Low-stock thresholds are stored at the product level.
- `days_until_stockout` is calculated using a helper method that divides current stock by average daily sales velocity.

---

## 🛠 Tech Stack & Tools
- **Language:** Java 17
- **Framework:** Spring Boot / Spring Data JPA
- **Database:** PostgreSQL (Design)
- **Concept:** ACID Transactions, Relational Mapping, REST API Best Practices
