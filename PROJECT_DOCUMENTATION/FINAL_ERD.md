# Final Warehouse Management System ERD

```mermaid
erDiagram
    USER {
        BIGINT user_id PK
        VARCHAR name
        VARCHAR email UK
        VARCHAR password_hash
        DATETIME created_at
    }

    ROLE {
        INT role_id PK
        VARCHAR role_name UK
    }

    USER_ROLE {
        BIGINT user_id PK, FK
        INT role_id PK, FK
    }

    WAREHOUSE {
        BIGINT warehouse_id PK
        VARCHAR location
        INT capacity
        VARCHAR email UK
        BIGINT manager_id FK
    }

    CUSTOMER {
        BIGINT customer_id PK
        VARCHAR name
        VARCHAR address
        VARCHAR phone_num
        VARCHAR email UK
        BIGINT user_id FK, UK
    }

    SUPPLIER {
        BIGINT supplier_id PK
        VARCHAR s_name
        VARCHAR contact_person
        VARCHAR email UK
    }

    PRODUCT {
        BIGINT product_id PK
        VARCHAR name
        TEXT description
        DECIMAL price
        VARCHAR category
        BIGINT supplier_id FK
    }

    INVENTORY {
        BIGINT inventory_id PK
        BIGINT product_id FK
        BIGINT warehouse_id FK
        INT quantity
        VARCHAR location_in_warehouse
    }

    ORDER {
        BIGINT order_id PK
        BIGINT customer_id FK
        DATETIME order_date
        DATETIME delivery_date
        DECIMAL total_amount
        VARCHAR status
    }

    ORDER_ITEM {
        BIGINT order_item_id PK
        BIGINT order_id FK
        BIGINT product_id FK
        INT quantity
        DECIMAL price_per_unit
    }

    SHIPMENT {
        BIGINT shipment_id PK
        BIGINT order_id FK
        BIGINT warehouse_id FK
        DATETIME shipment_date
        VARCHAR tracking_number UK
        VARCHAR status
        BIGINT picker_id FK
        BIGINT packer_id FK
    }

    PAYMENT {
        BIGINT payment_id PK
        BIGINT order_id FK
        VARCHAR transaction_id UK
        DATETIME payment_date
        DECIMAL amount
        VARCHAR payment_method
        VARCHAR status
    }

    PURCHASE_ORDER {
        BIGINT po_id PK
        BIGINT supplier_id FK
        BIGINT warehouse_id FK
        DATETIME order_date
        DATETIME expected_delivery_date
        DECIMAL total_amount
        VARCHAR status
        BIGINT receiver_id FK
    }

    PURCHASE_ORDER_ITEM {
        BIGINT po_item_id PK
        BIGINT po_id FK
        BIGINT product_id FK
        INT quantity
        DECIMAL unit_price
    }

    USER ||--o{ USER_ROLE : has
    ROLE ||--o{ USER_ROLE : grants

    USER ||--o{ WAREHOUSE : manages
    USER o|--|| CUSTOMER : account_for

    CUSTOMER ||--o{ ORDER : places
    ORDER ||--o{ ORDER_ITEM : contains
    ORDER ||--o{ SHIPMENT : fulfilled_by
    ORDER ||--o{ PAYMENT : paid_by

    PRODUCT ||--o{ ORDER_ITEM : ordered_as

    WAREHOUSE ||--o{ INVENTORY : stores
    PRODUCT ||--o{ INVENTORY : stocked_as

    SUPPLIER ||--o{ PRODUCT : supplies

    SUPPLIER ||--o{ PURCHASE_ORDER : receives
    WAREHOUSE ||--o{ PURCHASE_ORDER : for_warehouse
    PURCHASE_ORDER ||--o{ PURCHASE_ORDER_ITEM : contains
    PRODUCT ||--o{ PURCHASE_ORDER_ITEM : requested_as

    USER ||--o{ PURCHASE_ORDER : receives_at_dock
    WAREHOUSE ||--o{ SHIPMENT : ships_from
    USER ||--o{ SHIPMENT : picks
    USER ||--o{ SHIPMENT : packs
```

## Notes
- `USER_ROLE` is a composite-key mapping table (`user_id`, `role_id`).
- `CUSTOMER.user_id` is optional and unique to support guest customers and registered customers.
- `INVENTORY` enforces a unique pair on (`product_id`, `warehouse_id`).
- Operational responsibility links are explicit:
  - `WAREHOUSE.manager_id`
  - `PURCHASE_ORDER.receiver_id`
  - `SHIPMENT.picker_id`
  - `SHIPMENT.packer_id`
