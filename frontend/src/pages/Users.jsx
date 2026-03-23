import { useState, useEffect } from 'react';
import userService from '../services/userService';
import UserModal from '../components/UserModal';
import './Users.css';

const Users = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await userService.getAll();
      setUsers(response.data || []);
      setError('');
    } catch (err) {
      setError('Failed to load users');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this user?')) {
      return;
    }

    try {
      await userService.delete(id);
      fetchUsers();
    } catch (err) {
      alert('Failed to delete user');
    }
  };

  const handleStatusToggle = async (user) => {
    const newStatus = user.active ? false : true;
    try {
      await userService.updateStatus(user.userId, newStatus);
      fetchUsers();
    } catch (err) {
      alert('Failed to update user status');
    }
  };

  const handleEdit = (user) => {
    setSelectedUser(user);
    setShowModal(true);
  };

  const handleCreate = () => {
    setSelectedUser(null);
    setShowModal(true);
  };

  const handleModalClose = () => {
    setShowModal(false);
    setSelectedUser(null);
    fetchUsers();
  };

  if (loading && users.length === 0) {
    return <div className="loading">Loading users...</div>;
  }

  return (
    <div className="users-container">
      <div className="users-header">
        <h1>User Management</h1>
        <button onClick={handleCreate} className="btn-primary">
          Add User
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {users.length === 0 ? (
        <div className="empty-state">
          <p>No users found</p>
        </div>
      ) : (
        <div className="users-table-container">
          <table className="users-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Roles</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>
                    <div className="user-name-cell">
                      {user.name}
                    </div>
                  </td>
                  <td>{user.email}</td>
                  <td>-</td>
                  <td>
                    <div className="roles-cell">
                      {user.roles?.map((role, index) => (
                        <span key={index} className="role-badge">
                          {role}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td>
                    <span className={`status-badge ${user.active ? 'active' : 'inactive'}`}>
                      {user.active ? 'ACTIVE' : 'INACTIVE'}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        onClick={() => handleEdit(user)}
                        className="btn-icon"
                        title="Edit"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleStatusToggle(user)}
                        className="btn-icon"
                        title="Toggle Status"
                      >
                        {user.active ? 'Deactivate' : 'Activate'}
                      </button>
                      <button
                        onClick={() => handleDelete(user.userId)}
                        className="btn-icon btn-danger"
                        title="Delete"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <UserModal
          user={selectedUser}
          onClose={handleModalClose}
        />
      )}
    </div>
  );
};

export default Users;
