# 🧾 StockFlow - B2B Inventory Management System

**Backend Case Study Submission**

---

## 📌 Project Overview

StockFlow is a B2B SaaS platform designed for small businesses to manage inventory across multiple warehouses and maintain supplier relationships.

This submission focuses on:

* Debugging an existing API
* Designing a scalable database schema
* Implementing production-ready backend logic

---

# 🛠 Part 1: Code Review & Debugging

## 🔍 Issues Identified

### 1. Lack of Atomicity (Transaction Issue)

* The original implementation used two separate `commit()` calls.

### 2. Missing Input Validation

* Direct access to request fields without validation.

### 3. SKU Uniqueness Not Enforced

* No check for duplicate SKUs.

### 4. No Error Handling

* Database failures were not handled.

### 5. Unsafe Data Access

* Usage of `data['field']` could crash API if key is missing.

### 6. Price & Quantity Not Validated

* Invalid or negative values could be stored.

### 7. Rigid Warehouse Coupling

* Product creation tightly coupled with a single warehouse.

---

## ⚠️ Impact in Production

* ❌ Partial data creation (product without inventory)
* ❌ API crashes (500 errors due to missing fields)
* ❌ Duplicate SKUs → operational confusion
* ❌ Invalid financial data (negative/incorrect price)
* ❌ Poor scalability for multi-warehouse systems

---

## ✅ Corrected Implementation (Flask)

```python
from flask import request, jsonify
from sqlalchemy.exc import IntegrityError, SQLAlchemyError

@app.route('/api/products', methods=['POST'])
def create_product():
    data = request.get_json(silent=True)

    if not data:
        return jsonify({"error": "Invalid or missing JSON payload"}), 400

    # Extract fields safely
    name = data.get('name')
    sku = data.get('sku')
    price = data.get('price')
    warehouse_id = data.get('warehouse_id')
    quantity = data.get('initial_quantity', 0)

    # Basic validation
    if not name or not sku:
        return jsonify({"error": "Both 'name' and 'sku' are required"}), 400

    if price is None:
        return jsonify({"error": "Price is required"}), 400

    # Type & value validation
    try:
        price = float(price)
        if price <= 0:
            raise ValueError
    except (TypeError, ValueError):
        return jsonify({"error": "Price must be a positive number"}), 400

    try:
        quantity = int(quantity)
        if quantity < 0:
            raise ValueError
    except (TypeError, ValueError):
        return jsonify({"error": "Initial quantity must be a non-negative integer"}), 400

    try:
        # Check SKU uniqueness
        existing = Product.query.filter_by(sku=sku).first()
        if existing:
            return jsonify({"error": "SKU already exists"}), 409

        # Create product
        product = Product(
            name=name.strip(),
            sku=sku.strip(),
            price=price
        )

        db.session.add(product)
        db.session.flush()  # Get ID without committing

        # Optional inventory creation
        if warehouse_id:
            inventory = Inventory(
                product_id=product.id,
                warehouse_id=warehouse_id,
                quantity=quantity
            )
            db.session.add(inventory)

        # Single atomic commit
        db.session.commit()

        return jsonify({
            "message": "Product created successfully",
            "product_id": product.id
        }), 201

    except IntegrityError:
        db.session.rollback()
        return jsonify({"error": "Duplicate SKU or constraint violation"}), 409

    except SQLAlchemyError:
        db.session.rollback()
        return jsonify({"error": "Database error"}), 500
```

---

## 🔍 Explanation of Fixes

* ✔ Used safe `.get()` instead of direct dictionary access
* ✔ Added strong validation for required fields
* ✔ Ensured **SKU uniqueness** at application level
* ✔ Used **single transaction (commit)** to prevent partial data
* ✔ Used `flush()` to generate ID before commit
* ✔ Added proper **error handling with rollback**
* ✔ Allowed flexible inventory creation (multi-warehouse ready)

---

# 🗄 Part 2: Database Design

## 📊 Entity Overview

| Table          | Key Columns                          | Purpose                     |
| -------------- | ------------------------------------ | --------------------------- |
| **Companies**  | `id, name`                           | Represents business clients |
| **Warehouses** | `id, company_id, location`           | Stores inventory locations  |
| **Products**   | `id, sku (Unique), price, threshold` | Product master data         |
| **Inventory**  | `product_id, warehouse_id, qty`      | Stock per warehouse         |
| **Bundles**    | `parent_id, child_id, quantity`      | Composite products          |
| **Suppliers**  | `id, name, contact_email`            | Vendor management           |

---

## 🧠 Design Decisions

* **Composite Key:** `(product_id, warehouse_id)` prevents duplicate stock entries
* **Decimal Pricing:** Ensures financial precision
* **Loose Coupling:** Products are independent of warehouses
* **Bundle Support:** Enables combo/kit products

---

## ❓ Open Questions

* Should alerts be based on:

  * Total stock (global) OR
  * Per warehouse stock?

* Are bundle products:

  * Stored separately OR
  * Calculated dynamically?

---

# 🚀 Part 3: API Implementation

## 📡 Endpoint

`GET /api/companies/{id}/alerts/low-stock`

## ⚙️ Logic

* Filters products where:

  ```
  current_stock <= threshold
  ```
* Uses joins to fetch:

  * Warehouse details
  * Supplier info
* Avoids N+1 queries

## 📌 Assumptions

* Sales data exists for stock prediction
* A helper function calculates stock-out days

---

# 🏗 Tech Stack

* **Language:** Python
* **Framework:** Flask
* **ORM:** SQLAlchemy
* **Database:** PostgreSQL
* **Concepts:** Transactions, REST APIs, Data Integrity

---

# 📌 Conclusion

The updated implementation improves:

* Data consistency
* Error handling
* Scalability for multi-warehouse systems

This solution reflects a production-ready approach with proper validation, transactional integrity, and clean API design.
