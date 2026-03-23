import { useState, useEffect } from 'react';
import productService from '../services/productService';
import './Modal.css';

const ProductModal = ({ product, onClose }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    category: '',
    supplierId: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (product) {
      setFormData({
        name: product.name || '',
        description: product.description || '',
        price: product.price || '',
        category: product.category || '',
        supplierId: product.supplier?.supplierId || '',
      });
    }
  }, [product]);

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
        ...formData,
        price: parseFloat(formData.price),
        supplierId: parseInt(formData.supplierId),
      };

      if (product) {
        await productService.update(product.productId, submitData);
      } else {
        await productService.create(submitData);
      }
      onClose();
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Operation failed';
      const errorData = err.response?.data?.data;
      
      if (errorData && typeof errorData === 'object') {
        // Display validation errors
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
          <h2>{product ? 'Edit Product' : 'Add Product'}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label htmlFor="name">Product Name</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              placeholder="Enter product name"
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows="3"
              placeholder="Enter product description"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="price">Price ($)</label>
              <input
                type="number"
                id="price"
                name="price"
                value={formData.price}
                onChange={handleChange}
                required
                step="0.01"
                min="0.01"
                placeholder="0.00"
              />
            </div>

            <div className="form-group">
              <label htmlFor="category">Category</label>
              <input
                type="text"
                id="category"
                name="category"
                value={formData.category}
                onChange={handleChange}
                required
                placeholder="Enter category"
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="supplierId">Supplier ID</label>
            <input
              type="number"
              id="supplierId"
              name="supplierId"
              value={formData.supplierId}
              onChange={handleChange}
              required
              min="1"
              placeholder="Enter supplier ID"
            />
            <small style={{ color: '#e53e3e', fontSize: '12px', fontWeight: '500' }}>
              Important: You must create a supplier first. Use supplier ID 1 for testing if you have created one.
            </small>
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Saving...' : product ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProductModal;
