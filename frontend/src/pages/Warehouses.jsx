import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import warehouseService from '../services/warehouseService';
import WarehouseModal from '../components/WarehouseModal';
import './Warehouses.css';

const Warehouses = () => {
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedWarehouse, setSelectedWarehouse] = useState(null);
  const { user } = useAuth();

  const userRoles = user?.roles || [];
  const isAdmin = userRoles.includes('ADMIN');

  useEffect(() => {
    fetchWarehouses();
  }, []);

  const fetchWarehouses = async () => {
    try {
      setLoading(true);
      const response = await warehouseService.getAll();
      setWarehouses(response.data || []);
      setError('');
    } catch (err) {
      setError('Failed to load warehouses');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this warehouse?')) {
      return;
    }

    try {
      await warehouseService.delete(id);
      fetchWarehouses();
    } catch (err) {
      alert('Failed to delete warehouse');
    }
  };

  const handleEdit = (warehouse) => {
    setSelectedWarehouse(warehouse);
    setShowModal(true);
  };

  const handleCreate = () => {
    setSelectedWarehouse(null);
    setShowModal(true);
  };

  const handleModalClose = () => {
    setShowModal(false);
    setSelectedWarehouse(null);
    fetchWarehouses();
  };

  if (loading && warehouses.length === 0) {
    return <div className="loading">Loading warehouses...</div>;
  }

  return (
    <div className="warehouses-container">
      <div className="warehouses-header">
        <h1>Warehouses</h1>
        {isAdmin && (
          <button onClick={handleCreate} className="btn-primary">
            Add Warehouse
          </button>
        )}
      </div>

      {error && <div className="error-message">{error}</div>}

      {warehouses.length === 0 ? (
        <div className="empty-state">
          <p>No warehouses found</p>
        </div>
      ) : (
        <div className="warehouses-grid">
          {warehouses.map((warehouse) => (
            <div key={warehouse.id} className="warehouse-card">
              <div className="warehouse-header">
                <h3>{warehouse.name}</h3>
                <span className="warehouse-code">{warehouse.code}</span>
              </div>
              
              <div className="warehouse-location">
                <p>{warehouse.address}</p>
                <p>{warehouse.city}, {warehouse.state} {warehouse.zipCode}</p>
                <p>{warehouse.country}</p>
              </div>

              <div className="warehouse-details">
                <div className="detail-item">
                  <span className="label">Capacity:</span>
                  <span className="value">{warehouse.capacity} units</span>
                </div>
                {warehouse.manager && (
                  <div className="detail-item">
                    <span className="label">Manager:</span>
                    <span className="value">
                      {warehouse.manager.firstName} {warehouse.manager.lastName}
                    </span>
                  </div>
                )}
              </div>

              {isAdmin && (
                <div className="warehouse-actions">
                  <button onClick={() => handleEdit(warehouse)} className="btn-edit">
                    Edit
                  </button>
                  <button onClick={() => handleDelete(warehouse.id)} className="btn-delete">
                    Delete
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <WarehouseModal
          warehouse={selectedWarehouse}
          onClose={handleModalClose}
        />
      )}
    </div>
  );
};

export default Warehouses;
