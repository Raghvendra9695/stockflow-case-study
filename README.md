# StockFlow - B2B Inventory Management System
**Backend Case Study Submission**

## 📌 Project Overview
StockFlow is a B2B SaaS platform designed for small businesses to manage inventory across multiple warehouses and maintain supplier relationships. This repository contains the solution for the backend engineering case study, including code debugging, database design, and API implementation.

---

## 🛠 Part 1: Code Review & Debugging
### Identified Issues & Impact
1. **Lack of Atomicity (Transaction Risk):**
   - **Issue:** The original code used two separate `db.session.commit()` calls.
   - **Impact:** If the second commit fails, a product is created without an inventory record, leading to data corruption and "ghost products."
2. **Missing Input Validation:**
   - **Issue:** No check for mandatory fields like `sku` or `price`.
   - **Impact:** The API would crash with a `500 Internal Server Error` if a key was missing in the JSON request.
3. **Data Integrity (SKU Uniqueness):**
   - **Issue:** SKUs must be unique across the platform, but the code didn't check for duplicates.
   - **Impact:** Duplicate SKUs would cause operational confusion and database constraint violations.

### The Fix
- Implemented `@Transactional` logic (Java) to ensure both Product and Inventory are saved together or not at all.
- Added explicit validation and error handling for `IntegrityError` (Unique SKU).

---

## 🗄 Part 2: Database Design
### Entity Relationship Summary
The schema is designed to support multi-warehouse storage and product bundling (Composite Products).

| Table | Key Columns | Purpose |
|-------|-------------|---------|
| **Companies** | `id, name` | High-level B2B client entity. |
| **Warehouses** | `id, company_id, location` | Storage locations belonging to a company. |
| **Products** | `id, sku (Unique), price, threshold` | Core product details and alert levels. |
| **Inventory** | `product_id, warehouse_id, qty` | Tracks stock per product per warehouse. |
| **Bundles** | `parent_id, child_id, quantity` | Mapping for "Combo" or "Kit" products. |
| **Suppliers** | `id, name, contact_email` | Vendor information for reordering. |

### Design Decisions
- **Composite Primary Key:** Used `(product_id, warehouse_id)` in the Inventory table to prevent duplicate stock rows.
- **Decimal Type:** Used `Decimal(10,2)` for prices to ensure financial accuracy.
- **Self-Referencing Table:** The `Bundles` table allows a product to be composed of other products.

### Missing Requirements (Questions for Product Team)
- Should low-stock alerts be triggered based on **Total Stock** (Company-wide) or **Local Stock** (Warehouse-specific)?
- For **Bundles**, do we track the stock of the "Bundle SKU" itself, or is it calculated dynamically from child components?

---

## 🚀 Part 3: API Implementation
### Endpoint: `GET /api/companies/{id}/alerts/low-stock`
**Assumptions & Logic:**
- **Sales Velocity:** I assumed a helper method `calculate_days_until_stockout` exists, which uses the last 30 days of sales data to predict exhaustion.
- **Filtering:** The API only returns products where `current_stock <= low_stock_threshold`.
- **Joins:** Efficient SQL Joins are used to fetch Supplier and Warehouse names in a single request to avoid N+1 query problems.

---

## 🏗 Tech Stack Used
- **Language:** Java 17
- **Framework:** Spring Boot / Spring Data JPA
- **Database:** PostgreSQL (Schema Design)
- **Concepts:** ACID Transactions, RESTful API Design, Relational Modeling
