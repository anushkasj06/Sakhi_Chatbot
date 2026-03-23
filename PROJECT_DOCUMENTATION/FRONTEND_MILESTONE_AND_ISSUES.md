# Frontend Milestone & Issues (Based on Current Backend APIs)

Date: 2026-03-21

This file is the source-of-truth for creating the **Frontend** milestone and its issues.

It is based on:
- Implemented controllers in `backend/src/main/java/com/wms/controllers/`
- Current response envelope and error model
- Current JWT claim structure

## 1) Backend API Snapshot (Implemented)

Base URL: `http://localhost:8080/api/v1`

### 1.1 Response envelope
All endpoints return:

```json
{ "success": true|false, "message": "...", "data": <payload-or-null> }
```

Validation errors return `success=false`, `message="Validation failed"`, and `data` as a `field -> message` map.

### 1.2 Auth model
- Header: `Authorization: Bearer <accessToken>`
- Access token claims:
  - `sub` = userId (string)
  - `email`
  - `roles` = string array (e.g., `["ADMIN","WAREHOUSE_MANAGER"]`)
  - `type` = `"access"`
- Refresh token claims:
  - `jti` = tokenId (string)
  - `type` = `"refresh"`

Practical frontend implication: there is **no** `/me` endpoint yet, so role-based navigation should be derived by decoding the access token’s `roles` claim (backend remains the authority).

### 1.3 Implemented endpoints (by controller)

**Auth** (`/auth`)
- `POST /auth/login`
- `POST /auth/customer/signup`
- `POST /auth/refresh`
- `POST /auth/logout` (requires auth)
- `POST /auth/verify-email`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`

**System**
- `GET /ping` (requires auth)

**Products** (`/products`)
- `POST /products` (ADMIN, WAREHOUSE_MANAGER)
- `GET /products` (ADMIN, WAREHOUSE_MANAGER, CUSTOMER_SERVICE, CUSTOMER)
- `GET /products/{productId}` (same)
- `PUT /products/{productId}` (ADMIN, WAREHOUSE_MANAGER)
- `DELETE /products/{productId}` (ADMIN, WAREHOUSE_MANAGER)
- `GET /products/search?q=...` (same as list)
- `PATCH /products/{productId}/price` (ADMIN, WAREHOUSE_MANAGER)

**Warehouses** (`/warehouses`)
- `POST /warehouses` (ADMIN)
- `GET /warehouses` (ADMIN, WAREHOUSE_MANAGER)
- `GET /warehouses/{warehouseId}` (ADMIN, WAREHOUSE_MANAGER)
- `PUT /warehouses/{warehouseId}` (ADMIN)
- `PATCH /warehouses/{warehouseId}/manager` (ADMIN)
- `GET /warehouses/{warehouseId}/manager` (ADMIN, WAREHOUSE_MANAGER)
- `DELETE /warehouses/{warehouseId}` (ADMIN)

**Users** (`/users`) (ADMIN only)
- `POST /users`
- `GET /users`
- `GET /users/{userId}`
- `PUT /users/{userId}`
- `PATCH /users/{userId}/status`
- `DELETE /users/{userId}`
- `POST /users/{userId}/roles`
- `DELETE /users/{userId}/roles/{roleId}`
- `GET /users/{userId}/managed-warehouses`

**Roles** (`/roles`) (ADMIN only)
- `GET /roles`
- `POST /roles`

**Tasks** (`/tasks`)
- `GET /tasks/me`
- `GET /tasks/picking`
- `GET /tasks/packing`
- `GET /tasks/receiving`

## 2) Milestones

### Milestone: Frontend v1 (Auth + Portals for Implemented APIs)

**Milestone name**: `Frontend v1`

**Goal**
Deliver a usable React UI for:
- Auth flows used by both internal users and customers
- Internal portal screens to operate the implemented Admin/Manager/Workforce APIs
- Customer-facing product browsing (since products are accessible to the `CUSTOMER` role)

**Definition of Done (milestone-level)**
- All v1 issues in Section 3 closed
- All screens use the real backend endpoints listed in Section 1.3
- Token refresh works end-to-end when the access token expires
- Role-based navigation is implemented (UI hides disallowed actions)

### Milestone: Frontend v2 (Spec Modules; blocked until backend controllers exist)

**Milestone name**: `Frontend v2`

**Status**: BLOCKED (backend endpoints not implemented in current repo)

**Goal**
Add UI modules from the system API spec once their backend controllers are implemented:
- Customers (profile + orders)
- Suppliers
- Inventory
- Purchase orders + receiving
- Orders + shipments
- Payments
- Reports + audit

**Suggested v2 issue titles (create later, once backend is ready)**
- Customer profile (`/customers/me`) screen
- Customer orders list/detail/cancel screens (`/customer/orders`)
- Supplier CRUD screens (`/suppliers`)
- Inventory views + adjustments/transfers (`/inventory`)
- Purchase orders + items + receiving flow (`/purchase-orders`)
- Internal order management screens (`/orders`)
- Shipments assignment + status workflow screens (`/shipments`)
- Payments intent/confirm + finance refund screens (`/payments`)
- Reports dashboards (`/reports/*`)
- Audit events viewer (`/audit/events`)

## 3) Issues to Create Under `Frontend v1`

Create the following GitHub issues and assign them to the `Frontend v1` milestone.

### FE-01 — Add routing + app structure
**Why**: The current frontend is a single placeholder component.

**Scope**
- Add React Router
- Add top-level route structure for public vs authenticated areas

**Acceptance criteria**
- Routes exist for: `/login`, `/signup`, `/verify-email`, `/forgot-password`, `/reset-password`, and an authenticated area (e.g., `/app/*`)
- Unknown routes show a simple “not found” message

### FE-02 — Add environment-based API base URL
**Scope**
- Add `VITE_API_BASE_URL` support

**Acceptance criteria**
- Dev defaults to `http://localhost:8080/api/v1`
- No API URL literals scattered in components (single configuration source)

### FE-03 — Implement API client wrapper (ApiResponse-aware)
**Scope**
- Central `apiClient` wrapper around `fetch`
- Parse `{ success, message, data }`
- Standardize error mapping for validation map responses

**Acceptance criteria**
- Components call backend through the wrapper only
- UI can display validation errors returned as a field-map

### FE-04 — Token storage + auth session lifecycle
**Scope**
- Store `accessToken`, `refreshToken`, `expiresInSeconds`
- Decode access token claims (`roles`, `sub`)

**Acceptance criteria**
- Refresh flow: on 401 / token expiry, call `POST /auth/refresh`, retry once
- Logout calls `POST /auth/logout` then clears tokens

### FE-05 — Login page (internal + customer)
**Endpoints**: `POST /auth/login`

**Acceptance criteria**
- Form fields match `LoginRequest`: `email`, `password`
- On success, user is routed into authenticated area
- Shows backend validation and error messages (from `ApiResponse.message`)

### FE-06 — Customer signup page
**Endpoints**: `POST /auth/customer/signup`

**Acceptance criteria**
- Fields match `CustomerSignupRequest`: `name`, `address`, `phoneNum`, `email`, `password`
- On success, user is logged in (backend returns tokens) and routed into authenticated area

### FE-07 — Email verification page
**Endpoints**: `POST /auth/verify-email`

**Acceptance criteria**
- Route supports token coming from link (e.g., query string)
- Sends body `{ token }`
- Shows success/failure state from backend

### FE-08 — Forgot password page
**Endpoints**: `POST /auth/forgot-password`

**Acceptance criteria**
- Field matches `ForgotPasswordRequest`: `email`
- UI always shows a neutral confirmation message (backend does the same)

### FE-09 — Reset password page
**Endpoints**: `POST /auth/reset-password`

**Acceptance criteria**
- Route supports token coming from link
- Fields match `ResetPasswordRequest`: `token`, `newPassword`

### FE-10 — Authenticated app shell + role-based navigation
**Scope**
- Layout with navigation/menu
- Role-based visibility using decoded `roles` claim

**Acceptance criteria**
- If role is missing, hide related nav items and disable related actions
- Protected routes redirect to `/login` when unauthenticated

### FE-11 — Products: list + search + details
**Endpoints**: `GET /products`, `GET /products/search?q=...`, `GET /products/{productId}`

**Acceptance criteria**
- List screen supports search input calling `/products/search`
- Details screen shows fields from `ProductResponse`

### FE-12 — Products: create + update + delete
**Endpoints**: `POST /products`, `PUT /products/{productId}`, `DELETE /products/{productId}`

**Acceptance criteria**
- Form fields match `CreateProductRequest`/`UpdateProductRequest`
- Actions only available for `ADMIN` and `WAREHOUSE_MANAGER`

### FE-13 — Products: update price shortcut
**Endpoints**: `PATCH /products/{productId}/price`

**Acceptance criteria**
- A minimal “update price” action uses `UpdateProductPriceRequest` (`price`)
- Only available for `ADMIN` and `WAREHOUSE_MANAGER`

### FE-14 — Warehouses: list + details
**Endpoints**: `GET /warehouses`, `GET /warehouses/{warehouseId}`

**Acceptance criteria**
- Show `WarehouseResponse` fields
- Access limited to `ADMIN` and `WAREHOUSE_MANAGER`

### FE-15 — Warehouses: admin CRUD + manager assignment
**Endpoints**: `POST /warehouses`, `PUT /warehouses/{warehouseId}`, `DELETE /warehouses/{warehouseId}`, `PATCH /warehouses/{warehouseId}/manager`, `GET /warehouses/{warehouseId}/manager`

**Acceptance criteria**
- Admin-only screens/actions
- Manager assignment takes `{ managerId }` body

### FE-16 — Admin: users management
**Endpoints**: `GET /users`, `POST /users`, `PUT /users/{userId}`, `PATCH /users/{userId}/status`, `DELETE /users/{userId}`, `GET /users/{userId}`

**Acceptance criteria**
- Admin-only
- Create user fields match `CreateUserRequest` (including initial `roles` array)
- Status toggle uses `UpdateUserStatusRequest` (`active`)

### FE-17 — Admin: roles management + assign/remove user roles
**Endpoints**: `GET /roles`, `POST /roles`, `POST /users/{userId}/roles`, `DELETE /users/{userId}/roles/{roleId}`

**Acceptance criteria**
- Admin-only
- Create role uses `CreateRoleRequest` (`roleName`)
- Assign role uses `AssignRoleRequest` (`roleName`)

### FE-18 — Workforce: task lists
**Endpoints**: `GET /tasks/me`, `GET /tasks/picking`, `GET /tasks/packing`, `GET /tasks/receiving`

**Acceptance criteria**
- Screen(s) show `WorkforceTaskResponse` fields
- Only show task categories the current role is allowed to call

### FE-19 — Connectivity check (authenticated ping)
**Endpoints**: `GET /ping`

**Acceptance criteria**
- App makes a small authenticated call after login (or on app boot) to detect expired sessions and trigger refresh/logout behavior

## 4) Notes

- Only endpoints listed in Section 1.3 are currently implementable end-to-end.
- The v2 milestone is intentionally marked BLOCKED until backend controllers exist in the repo.
