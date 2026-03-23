import { useState, useEffect } from 'react';
import userService from '../services/userService';
import './Modal.css';

const UserModal = ({ user, onClose }) => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    active: true,
    roles: [],
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const availableRoles = [
    'ADMIN',
    'WAREHOUSE_MANAGER',
    'RECEIVING_CLERK',
    'PICKER',
    'PACKER',
    'CUSTOMER_SERVICE',
    'FINANCE',
    'AUDITOR',
    'CUSTOMER',
  ];

  useEffect(() => {
    if (user) {
      setFormData({
        name: user.name || '',
        email: user.email || '',
        password: '',
        active: user.active !== undefined ? user.active : true,
        roles: user.roles || [],
      });
    }
  }, [user]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({ 
      ...formData, 
      [name]: type === 'checkbox' ? checked : value 
    });
    setError('');
  };

  const handleRoleChange = (e) => {
    const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
    setFormData({ ...formData, roles: selectedOptions });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const submitData = { ...formData };
      if (user && !submitData.password) {
        delete submitData.password;
      }

      if (user) {
        await userService.update(user.userId, submitData);
      } else {
        await userService.create(submitData);
      }
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Operation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{user ? 'Edit User' : 'Add User'}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label htmlFor="name">Full Name</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              placeholder="Enter full name"
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
              placeholder="Enter email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">
              Password {user && '(leave blank to keep current)'}
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required={!user}
              minLength="8"
              placeholder={user ? 'Leave blank to keep current' : 'Enter password'}
            />
          </div>

          <div className="form-group">
            <label htmlFor="roles">Roles</label>
            <select
              id="roles"
              name="roles"
              multiple
              value={formData.roles}
              onChange={handleRoleChange}
              required
              size="5"
              style={{ height: 'auto' }}
            >
              {availableRoles.map((role) => (
                <option key={role} value={role}>
                  {role}
                </option>
              ))}
            </select>
            <small style={{ color: '#718096', fontSize: '12px' }}>
              Hold Ctrl (Cmd on Mac) to select multiple roles
            </small>
          </div>

          <div className="form-group">
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <input
                type="checkbox"
                name="active"
                checked={formData.active}
                onChange={handleChange}
              />
              Active
            </label>
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Saving...' : user ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UserModal;
