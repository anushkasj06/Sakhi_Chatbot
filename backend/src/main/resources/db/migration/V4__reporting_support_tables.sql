CREATE TABLE inventory_movements (
  movement_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inventory_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  quantity_delta INT NOT NULL,
  movement_type VARCHAR(50) NOT NULL,
  reason VARCHAR(255),
  reference_type VARCHAR(100),
  reference_id VARCHAR(100),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT,
  FOREIGN KEY (inventory_id) REFERENCES inventory(inventory_id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
  FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id) ON DELETE CASCADE,
  FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_inventory_movements_created_at ON inventory_movements(created_at);
CREATE INDEX idx_inventory_movements_warehouse_id ON inventory_movements(warehouse_id);
CREATE INDEX idx_inventory_movements_product_id ON inventory_movements(product_id);

CREATE TABLE audit_events (
  event_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_type VARCHAR(100) NOT NULL,
  entity_type VARCHAR(100),
  entity_id VARCHAR(100),
  action VARCHAR(100) NOT NULL,
  performed_by BIGINT,
  details TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (performed_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_events_created_at ON audit_events(created_at);
CREATE INDEX idx_audit_events_event_type ON audit_events(event_type);
