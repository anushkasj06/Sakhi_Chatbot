import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import productService from '../services/productService';
import ProductModal from '../components/ProductModal';
import './Products.css';

const Products = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const { user } = useAuth();

  const userRoles = user?.roles || [];
  const canManage = userRoles.includes('ADMIN') || userRoles.includes('WAREHOUSE_MANAGER');

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await productService.getAll();
      setProducts(response.data || []);
      setError('');
    } catch (err) {
      setError('Failed to load products');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) {
      fetchProducts();
      return;
    }

    try {
      setLoading(true);
      const response = await productService.search(searchQuery);
      setProducts(response.data || []);
    } catch (err) {
      setError('Search failed');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this product?')) {
      return;
    }

    try {
      await productService.delete(id);
      fetchProducts();
    } catch (err) {
      alert('Failed to delete product');
    }
  };

  const handlePriceUpdate = async (product) => {
    const newPrice = prompt(`Update price for "${product.name}"\nCurrent price: $${product.price}\n\nEnter new price:`);
    
    if (newPrice === null) return; // User cancelled
    
    const price = parseFloat(newPrice);
    if (isNaN(price) || price <= 0) {
      alert('Please enter a valid price');
      return;
    }

    try {
      await productService.updatePrice(product.productId, price);
      fetchProducts();
    } catch (err) {
      alert('Failed to update price');
    }
  };

  const handleEdit = (product) => {
    setSelectedProduct(product);
    setShowModal(true);
  };

  const handleCreate = () => {
    setSelectedProduct(null);
    setShowModal(true);
  };

  const handleModalClose = () => {
    setShowModal(false);
    setSelectedProduct(null);
    fetchProducts();
  };

  if (loading && products.length === 0) {
    return <div className="loading">Loading products...</div>;
  }

  return (
    <div className="products-container">
      <div className="products-header">
        <h1>Products</h1>
        <div className="products-actions">
          <form onSubmit={handleSearch} className="search-form">
            <input
              type="text"
              placeholder="Search products..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="search-input"
            />
            <button type="submit" className="btn-search">Search</button>
          </form>
          {canManage && (
            <button onClick={handleCreate} className="btn-primary">
              Add Product
            </button>
          )}
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {products.length === 0 ? (
        <div className="empty-state">
          <p>No products found</p>
        </div>
      ) : (
        <div className="products-grid">
          {products.map((product) => (
            <div key={product.productId} className="product-card">
              <div className="product-header">
                <h3>{product.name}</h3>
                <span className="product-sku">{product.category}</span>
              </div>
              <p className="product-description">{product.description || 'No description'}</p>
              <div className="product-details">
                <div className="detail-item">
                  <span className="label">Price:</span>
                  <span className="value">${product.price}</span>
                </div>
                <div className="detail-item">
                  <span className="label">Category:</span>
                  <span className="value">{product.category}</span>
                </div>
                <div className="detail-item">
                  <span className="label">Supplier:</span>
                  <span className="value">{product.supplierName || 'N/A'}</span>
                </div>
              </div>
              {canManage && (
                <div className="product-actions">
                  <button onClick={() => handlePriceUpdate(product)} className="btn-price">
                    Update Price
                  </button>
                  <button onClick={() => handleEdit(product)} className="btn-edit">
                    Edit
                  </button>
                  <button onClick={() => handleDelete(product.productId)} className="btn-delete">
                    Delete
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <ProductModal
          product={selectedProduct}
          onClose={handleModalClose}
        />
      )}
    </div>
  );
};

export default Products;
