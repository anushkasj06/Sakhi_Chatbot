# Warehouse Management System (WMS)
## Final System Design, Process Flow, Data Flow, and Backend API Specification

Version: 1.0 (Final after latest ERD)
Date: 2026-03-15

## 1. Purpose and Scope
This document defines the final target design of the Warehouse Management System based on the latest ERD and discussions.

It covers:
- End-to-end system architecture
- Actors, roles, and portal behavior
- Functional modules
- Process flows and data flows
- Full backend API catalog (required endpoints)
- Security model and access control
- Validation and error conventions

This is the implementation blueprint for backend and frontend teams.

## 2. Final ERD Reference
Authoritative ERD source:
- erdv2.png (same folder)

Core entities:
- users, roles, user_roles
- customers, suppliers, products
- warehouses, inventory
- orders, order_items
- shipments
- payments
- purchase_orders, purchase_order_items

Operational assignment links:
- warehouses.manager_id -> users.user_id
- purchase_orders.receiver_id -> users.user_id
- shipments.picker_id -> users.user_id
- shipments.packer_id -> users.user_id

## 3. Business Actors and Access Model

### 3.1 Internal Users
Internal users log in to the internal portal. Accounts are created by Admin (no public signup).

Roles:
- ADMIN
- WAREHOUSE_MANAGER
- RECEIVING_CLERK
- PICKER
- PACKER
- CUSTOMER_SERVICE
- FINANCE
- AUDITOR (read-only)

### 3.2 External Customer
Customers use customer portal.
- Customer can signup/login.
- Customer profile maps to customers.user_id (optional and unique).
- Guest checkout allowed: customer record without linked user account.

### 3.3 Supplier
Current final decision:
- No supplier login portal in v1.
- Supplier data and interactions are managed by internal users.
- Purchase orders are communicated externally (email/integration), not via supplier dashboard.

## 4. System Architecture

### 4.1 High-Level Components
- Frontend (React/Vite)
  - Internal Portal UI
  - Customer Portal UI
- Backend (Spring Boot)
  - REST APIs
  - Authentication + Authorization
  - Business services
  - Data access layer
- Database (MySQL)
  - ERD schema as final source of truth
- External Integrations
  - Payment gateway (Stripe/PayPal/Adyen style)
  - Email/SMS notification provider

### 4.2 Layered Backend Design
- Controller Layer: REST endpoints, request/response DTO mapping
- Service Layer: domain logic, transactions, orchestration
- Repository Layer: persistence operations
- Security Layer: JWT auth + RBAC + warehouse-scoped access checks

### 4.3 Key Design Principles
- Suppliers are managed internally
- Sensitive card data never stored in WMS DB
- Inventory is warehouse-specific and unique by (product_id, warehouse_id)
- Role + context based authorization (role plus assigned warehouse/task)

## 5. Functional Modules

### 5.1 Identity and Access Management
- User lifecycle for employees
- Customer account signup/login
- Role assignment
- Token issuance and refresh
- Password reset flow

### 5.2 Master Data Management
- Warehouses
- Suppliers
- Products
- Customer profiles

### 5.3 Inventory Management
- Stock visibility by warehouse
- Stock adjustment and transfer
- Location-level tracking (location_in_warehouse)
- Low-stock alerts

### 5.4 Procurement and Receiving
- Purchase order creation/approval
- Purchase order item management
- Receiving workflow and receiver assignment
- Inventory increment on receipt

### 5.5 Sales Order Management
- Customer order creation
- Order items and totals
- Status lifecycle: PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED/CANCELLED

### 5.6 Fulfillment and Shipping
- Shipment creation from order
- Picker/packer assignment
- Tracking number and status updates
- Inventory decrement during fulfillment

### 5.7 Payments
- Payment intent and confirmation through gateway
- Persist transaction_id and status only
- Reconciliation support for Finance

### 5.8 Reporting and Audit
- Inventory reports
- Order and shipment reports
- Purchase order and supplier reports
- Payment reconciliation reports
- Audit trail events

## 6. End-to-End Process Flows

### 6.1 User and Role Setup (Admin)
1. Admin creates internal user.
2. Admin assigns one or more roles in user_roles.
3. If role is WAREHOUSE_MANAGER, user is linked as warehouses.manager_id.
4. User logs in and receives role-scoped access.

### 6.2 Customer Lifecycle
1. Customer signs up (or uses guest checkout).
2. If signed up, user account is linked to customer profile.
3. Customer places order.
4. Customer tracks shipment and payment status.

### 6.3 Procurement to Stock-In
1. Warehouse Manager creates purchase order for supplier.
2. Purchase order moves through approval/submission states.
3. Receiving Clerk receives goods against PO.
4. receiver_id is recorded.
5. Inventory is increased in target warehouse.

### 6.4 Order to Delivery
1. Customer order is created with order items.
2. Stock reservation/availability check runs.
3. Shipment is created.
4. Picker and Packer are assigned.
5. Tracking number generated and shipment sent.
6. Status updated to DELIVERED when completed.

### 6.5 Payment Flow
1. Customer starts checkout.
2. Frontend redirects/calls payment gateway SDK.
3. Gateway handles card details directly.
4. Backend receives transaction reference and verifies.
5. payments record is created/updated.
6. Order status changes to CONFIRMED/PROCESSING if payment success.

## 7. Data Flow Design

### 7.1 Identity Data Flow
- Input: login/signup payload
- Processing: credential validation, JWT generation
- Output: access token, refresh token, role claims, scoped claims

### 7.2 Inventory Data Flow
- Input: receipt, order allocation, adjustment
- Processing: stock increment/decrement with transaction boundary
- Output: current stock snapshot and movement history

### 7.3 Fulfillment Data Flow
- Input: order lines, warehouse selection
- Processing: pick-pack-ship state machine
- Output: shipment status + tracking updates

### 7.4 Payment Data Flow
- Input: payment intent/transaction status from gateway
- Processing: verification and reconciliation
- Output: payment status and order payment state

## 8. Status Models

### 8.1 Order Status
- PENDING
- CONFIRMED
- PROCESSING
- SHIPPED
- DELIVERED
- CANCELLED
- RETURN_REQUESTED (optional v1.1)
- RETURNED (optional v1.1)

### 8.2 Shipment Status
- CREATED
- PICKING
- PACKING
- READY_TO_SHIP
- IN_TRANSIT
- DELIVERED
- FAILED_DELIVERY
- CANCELLED

### 8.3 Purchase Order Status
- DRAFT
- APPROVED
- SUBMITTED
- PARTIALLY_RECEIVED
- RECEIVED
- CANCELLED

### 8.4 Payment Status
- INITIATED
- AUTHORIZED
- CAPTURED
- FAILED
- REFUNDED
- PARTIALLY_REFUNDED

## 9. Backend API Standard

Base path: /api/v1

Headers:
- Authorization: Bearer <token> (except public auth/customer signup endpoints)
- X-Correlation-Id: optional for tracing

Common response envelope:
- success: boolean
- message: string
- data: object/array/null
- errors: array/null
- meta: pagination and trace metadata

## 10. Authentication and Authorization APIs

### 10.1 Public Auth
- POST /auth/login
  - Internal users and registered customers
- POST /auth/customer/signup
- POST /auth/customer/verify-email (optional)
- POST /auth/refresh
- POST /auth/forgot-password
- POST /auth/reset-password

### 10.2 Internal User Management (Admin)
- POST /users
- GET /users
- GET /users/{userId}
- PUT /users/{userId}
- PATCH /users/{userId}/status
- DELETE /users/{userId}

### 10.3 Role Management (Admin)
- GET /roles
- POST /roles
- POST /users/{userId}/roles
- DELETE /users/{userId}/roles/{roleId}

## 11. Warehouse and Workforce APIs

### 11.1 Warehouses
- POST /warehouses
- GET /warehouses
- GET /warehouses/{warehouseId}
- PUT /warehouses/{warehouseId}
- DELETE /warehouses/{warehouseId}

### 11.2 Warehouse Manager Assignment
- PATCH /warehouses/{warehouseId}/manager
- GET /warehouses/{warehouseId}/manager
- GET /users/{userId}/managed-warehouses

### 11.3 Workforce Task APIs
- GET /tasks/me
- GET /tasks/picking
- GET /tasks/packing
- GET /tasks/receiving

## 12. Customer APIs

### 12.1 Customer Profile
- GET /customers/me
- PUT /customers/me
- GET /customers/{customerId} (internal roles)
- GET /customers (internal roles)

### 12.2 Customer Orders
- POST /customer/orders
- GET /customer/orders
- GET /customer/orders/{orderId}
- POST /customer/orders/{orderId}/cancel

## 13. Supplier APIs (Internal Managed)
- POST /suppliers
- GET /suppliers
- GET /suppliers/{supplierId}
- PUT /suppliers/{supplierId}
- DELETE /suppliers/{supplierId}
- GET /suppliers/{supplierId}/products

## 14. Product APIs
- POST /products
- GET /products
- GET /products/{productId}
- PUT /products/{productId}
- DELETE /products/{productId}
- GET /products/search?q=...
- PATCH /products/{productId}/price

## 15. Inventory APIs
- GET /inventory
- GET /inventory/{inventoryId}
- GET /inventory/warehouse/{warehouseId}
- GET /inventory/product/{productId}
- POST /inventory/adjustments
- POST /inventory/transfers
- GET /inventory/low-stock

### 15.1 Low-Stock Alert APIs
- GET /alerts/low-stock
  - query params: status (optional), mineOnly (optional)
- POST /alerts/low-stock/{alertId}/acknowledge
- POST /alerts/low-stock/{alertId}/resolve
- POST /alerts/low-stock/scan
  - manual scan trigger (admin/warehouse-manager)

Adjustment payload includes:
- warehouseId
- productId
- quantityDelta (+/-)
- reason
- referenceType/referenceId

## 16. Purchase Order APIs

### 16.1 Purchase Orders
- POST /purchase-orders
- GET /purchase-orders
- GET /purchase-orders/{poId}
- PUT /purchase-orders/{poId}
- PATCH /purchase-orders/{poId}/status
- POST /purchase-orders/{poId}/submit
- POST /purchase-orders/{poId}/cancel

### 16.2 Purchase Order Items
- POST /purchase-orders/{poId}/items
- PUT /purchase-orders/{poId}/items/{poItemId}
- DELETE /purchase-orders/{poId}/items/{poItemId}

### 16.3 Receiving
- POST /purchase-orders/{poId}/receive
  - records receiver_id
  - supports partial or full receive
  - updates inventory

## 17. Order APIs (Internal + Customer-Service)

### 17.1 Internal Order Management
- POST /orders
- GET /orders
- GET /orders/{orderId}
- PUT /orders/{orderId}
- PATCH /orders/{orderId}/status
- POST /orders/{orderId}/cancel

### 17.2 Order Items
- POST /orders/{orderId}/items
- PUT /orders/{orderId}/items/{orderItemId}
- DELETE /orders/{orderId}/items/{orderItemId}

## 18. Shipment APIs

### 18.1 Shipment CRUD and Tracking
- POST /shipments
- GET /shipments
- GET /shipments/{shipmentId}
- PATCH /shipments/{shipmentId}/status
- PATCH /shipments/{shipmentId}/tracking

### 18.2 Assignment APIs
- PATCH /shipments/{shipmentId}/assign-picker
- PATCH /shipments/{shipmentId}/assign-packer
- GET /shipments/{shipmentId}/assignments

### 18.3 Task Completion
- POST /shipments/{shipmentId}/mark-picked
- POST /shipments/{shipmentId}/mark-packed
- POST /shipments/{shipmentId}/dispatch

## 19. Payment APIs

### 19.1 Payment Processing
- POST /payments/intent
- POST /payments/confirm
- GET /payments/{paymentId}
- GET /payments/order/{orderId}
- POST /payments/{paymentId}/refund (optional role FINANCE)

Important:
- Never accept/store raw card numbers, CVV, expiry in WMS database.
- Store only transaction_id, method abstraction, amount, status, timestamps.

## 20. Reporting APIs
- GET /reports/inventory-summary
- GET /reports/inventory-movements
- GET /reports/orders-summary
- GET /reports/shipments-summary
- GET /reports/purchase-orders-summary
- GET /reports/payments-reconciliation

Notes:
- Reporting APIs support optional filters as query params.
- Date filters use ISO date format (yyyy-MM-dd).

Filters:
- fromDate, toDate
- warehouseId
- status
- supplierId/customerId

## 21. Admin and Health APIs
- GET /health/live
- GET /health/ready
- GET /ping
- GET /audit/events

Notes:
- /health/ready validates database connectivity in addition to app liveness.
- /audit/events returns latest audit trail events for ADMIN/AUDITOR roles.

## 22. Role to Endpoint Access Matrix (High-Level)

- ADMIN
  - full access
- WAREHOUSE_MANAGER
  - warehouses, inventory, purchase orders, shipments, reports (warehouse-scoped)
- RECEIVING_CLERK
  - receive APIs, PO read, inventory read/write (receiving scoped)
- PICKER
  - assigned picking tasks and mark-picked actions
- PACKER
  - assigned packing tasks and mark-packed/dispatch actions
- CUSTOMER_SERVICE
  - customer support views, order creation/update, shipment tracking
- FINANCE
  - payments read, reconciliation, refund workflows
- AUDITOR
  - read-only reporting and audit logs
- CUSTOMER
  - own profile, own orders, own payments and tracking

## 23. Validation Rules

Examples:
- Email unique in users and customers
- customer.user_id unique when present
- tracking_number unique in shipments
- transaction_id unique in payments
- inventory unique on (product_id, warehouse_id)
- quantity cannot go negative unless explicitly configured
- status transitions must follow state machine

## 24. Error Model

Standard error object:
- code
- message
- field (optional)
- details (optional)

Common codes:
- AUTH_001 invalid_credentials
- AUTH_002 token_expired
- AUTH_003 access_denied
- VAL_001 validation_error
- INV_001 insufficient_stock
- ORD_001 invalid_order_status_transition
- PAY_001 payment_failed
- SYS_001 unexpected_error

## 25. Security and Compliance
- JWT access token + refresh token
- Passwords hashed with BCrypt/Argon2
- HTTPS only outside local dev
- CORS restricted to approved frontend origins
- Rate limiting on auth endpoints
- Audit logs for critical operations
- Payment PCI boundary handled by external gateway

## 26. Non-Functional Requirements
- Availability target: 99.9% (production)
- P95 API latency target: < 300 ms for normal reads
- Idempotency for critical write APIs (payments, dispatch, receiving)
- Pagination mandatory for list APIs
- Structured logging and correlation IDs

## 27. Suggested Backend Package Structure
- controllers/
- services/
- repositories/
- models/
- dtos/request/
- dtos/response/
- security/
- exceptions/
- enums/
- config/

## 28. Implementation Sequence (Recommended)
1. Security and auth foundation
2. Master data: users/roles/warehouses/suppliers/products
3. Inventory module
4. Purchase order + receiving
5. Customer + orders + order items
6. Shipment + assignment workflow
7. Payment integration
8. Reports + audit endpoints
9. Performance hardening + observability

## 29. Final Notes
- This API catalog is the required target design for full end-to-end implementation.
- Current repository has schema and entity model alignment completed.
- Controller/service/repository endpoint implementation is the next build phase.
