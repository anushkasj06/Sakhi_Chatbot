import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import supplierService from '../services/supplierService';
import SupplierModal from '../components/SupplierModal';
import './Suppliers.css';

const Suppliers = () => {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedSupplier, setSelectedSupplier] = useState(null);
  const { user } = useAuth();

  const userRoles = user?.roles || [];
  const canManage = userRoles.includes('ADMIN') || userRoles.includes('WAREHOUSE_MANAGER');

  useEffect(() => {
    fetchSuppliers();
  }, []);

  const fetchSuppliers = async () => {
    try {
      setLoading(true);
      const response = await supplierService.getAll();
      setSuppliers(response.data || []);
      setError('');
    } catch (err) {
      setError('Failed to load suppliers');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this supplier?')) {
      return;
    }

    try {
      await supplierService.delete(id);
      fetchSuppliers();
    } catch (err) {
      alert('Failed to delete supplier');
    }
  };

  const handleEdit = (supplier) => {
    setSelectedSupplier(supplier);
    setShowModal(true);
  };

  const handleCreate = () => {
    setSelectedSupplier(null);
    setShowModal(true);
  };

  const handleModalClose = () => {
    setShowModal(false);
    setSelectedSupplier(null);
    fetchSuppliers();
  };

  if (loading && suppliers.length === 0) {
    return <div className="loading">Loading suppliers...</div>;
  }

  return (
    <div className="suppliers-container">
      <div className="suppliers-header">
        <h1>Suppliers</h1>
        {canManage && (
          <button onClick={handleCreate} className="btn-primary">
            Add Supplier
          </button>
        )}
      </div>

      {error && <div className="error-message">{error}</div>}

      {suppliers.length === 0 ? (
        <div className="empty-state">
          <p>No suppliers found</p>
          {canManage && (
            <p style={{ marginTop: '8px', fontSize: '14px', color: '#718096' }}>
              Create your first supplier to start managing products
            </p>
          )}
        </div>
      ) : (
        <div className="suppliers-grid">
          {suppliers.map((supplier) => (
            <div key={supplier.supplierId} className="supplier-card">
              <div className="supplier-header">
                <h3>{supplier.sName}</h3>
                <span className="supplier-id">ID: {supplier.supplierId}</span>
              </div>
              
              <div className="supplier-details">
                <div className="detail-item">
                  <span className="label">Contact Person:</span>
                  <span className="value">{supplier.contactPerson}</span>
                </div>
                <div className="detail-item">
                  <span className="label">Email:</span>
                  <span className="value">{supplier.email}</span>
                </div>
              </div>

              {canManage && (
                <div className="supplier-actions">
                  <button onClick={() => handleEdit(supplier)} className="btn-edit">
                    Edit
                  </button>
                  <button onClick={() => handleDelete(supplier.supplierId)} className="btn-delete">
                    Delete
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <SupplierModal
          supplier={selectedSupplier}
          onClose={handleModalClose}
        />
      )}
    </div>
  );
};

export default Suppliers;
