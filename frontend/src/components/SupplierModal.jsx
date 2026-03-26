import { useState, useEffect } from 'react';
import supplierService from '../services/supplierService';
import './Modal.css';

const SupplierModal = ({ supplier, onClose }) => {
  const [formData, setFormData] = useState({
    sName: '',
    contactPerson: '',
    email: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (supplier) {
      setFormData({
        sName: supplier.sName || '',
        contactPerson: supplier.contactPerson || '',
        email: supplier.email || '',
      });
    }
  }, [supplier]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    console.log(`Field changed: ${name} = "${value}"`);
    setFormData({ ...formData, [name]: value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // Ensure all fields are trimmed and not empty
      const submitData = {
        sName: formData.sName.trim(),
        contactPerson: formData.contactPerson.trim(),
        email: formData.email.trim(),
      };

      // Validate before sending
      if (!submitData.sName || !submitData.contactPerson || !submitData.email) {
        setError('All fields are required');
        setLoading(false);
        return;
      }

      console.log('Submitting supplier data:', submitData);

      if (supplier) {
        await supplierService.update(supplier.supplierId, submitData);
      } else {
        await supplierService.create(submitData);
      }
      onClose();
    } catch (err) {
      console.error('Supplier submission error:', err);
      console.error('Error response:', err.response?.data);
      
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
          <h2>{supplier ? 'Edit Supplier' : 'Add Supplier'}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label htmlFor="sName">Supplier Name</label>
            <input
              type="text"
              id="sName"
              name="sName"
              value={formData.sName}
              onChange={handleChange}
              required
              placeholder="Enter supplier name"
            />
          </div>

          <div className="form-group">
            <label htmlFor="contactPerson">Contact Person</label>
            <input
              type="text"
              id="contactPerson"
              name="contactPerson"
              value={formData.contactPerson}
              onChange={handleChange}
              required
              placeholder="Enter contact person name"
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
              placeholder="Enter email address"
            />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Saving...' : supplier ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SupplierModal;
