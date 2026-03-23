CREATE TABLE low_stock_alerts (
  alert_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inventory_id BIGINT NOT NULL,
  threshold INT NOT NULL,
  current_quantity INT NOT NULL,
  status VARCHAR(50) NOT NULL,
  assigned_user_id BIGINT,
  acknowledged_by BIGINT,
  acknowledged_at DATETIME,
  resolved_at DATETIME,
  notification_count INT NOT NULL DEFAULT 0,
  last_notified_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (inventory_id) REFERENCES inventory(inventory_id) ON DELETE CASCADE,
  FOREIGN KEY (assigned_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
  FOREIGN KEY (acknowledged_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_low_stock_alert_status ON low_stock_alerts(status);
CREATE INDEX idx_low_stock_alert_inventory ON low_stock_alerts(inventory_id);
CREATE INDEX idx_low_stock_alert_assigned_user ON low_stock_alerts(assigned_user_id);
