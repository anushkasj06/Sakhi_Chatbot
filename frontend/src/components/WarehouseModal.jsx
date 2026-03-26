import { useState, useEffect } from 'react';
import warehouseService from '../services/warehouseService';
import userService from '../services/userService';
import './Modal.css';

const WarehouseModal = ({ warehouse, onClose }) => {
  const [formData, setFormData] = useState({
    location: '',
    capacity: '',
    email: '',
    managerId: '',
  });
  const [managers, setManagers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchManagers();
    if (warehouse) {
      setFormData({
        location: warehouse.location || '',
        capacity: warehouse.capacity || '',
        email: warehouse.email || '',
        managerId: warehouse.managerId || '',
      });
    }
  }, [warehouse]);

  const fetchManagers = async () => {
    try {
      const response = await userService.getAll();
      const allUsers = response.data || [];
      const managerUsers = allUsers.filter(user => 
        user.roles?.some(role => role === 'WAREHOUSE_MANAGER')
      );
      setManagers(managerUsers);
    } catch (err) {
      console.error('Failed to fetch managers:', err);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const submitData = {
        location: formData.location.trim(),
        capacity: parseInt(formData.capacity),
        email: formData.email.trim(),
        managerId: formData.managerId ? parseInt(formData.managerId) : null,
      };

      if (warehouse) {
        await warehouseService.update(warehouse.warehouseId, submitData);
      } else {
        await warehouseService.create(submitData);
      }
      onClose();
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Operation failed';
      const errorData = err.response?.data?.data;
      
      if (errorData && typeof errorData === 'object') {
        const validationErrors = Object.entries(errorData)
          .map(([field, message]) => `${field}: ${message}`)
          .join(', ');
        setError(`Validation failed: ${validationErrors}`);
      } else {
        setError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{warehouse ? 'Edit Warehouse' : 'Add Warehouse'}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label htmlFor="location">Location</label>
            <input
              type="text"
              id="location"
              name="location"
              value={formData.location}
              onChange={handleChange}
              required
              placeholder="Enter warehouse location"
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              placeholder="Enter warehouse email"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="capacity">Capacity</label>
              <input
                type="number"
                id="capacity"
                name="capacity"
                value={formData.capacity}
                onChange={handleChange}
                required
                min="1"
                placeholder="Enter capacity"
              />
            </div>

            <div className="form-group">
              <label htmlFor="managerId">Manager</label>
              <select
                id="managerId"
                name="managerId"
                value={formData.managerId}
                onChange={handleChange}
              >
                <option value="">Select Manager (Optional)</option>
                {managers.map((manager) => (
                  <option key={manager.userId} value={manager.userId}>
                    {manager.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Saving...' : warehouse ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default WarehouseModal;
