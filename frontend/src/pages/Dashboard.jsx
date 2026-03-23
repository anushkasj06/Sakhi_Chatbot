import { useAuth } from '../context/AuthContext';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();

  const userRoles = user?.roles || [];

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Dashboard</h1>
        <p>Welcome back, {user?.email}</p>
      </div>

      <div className="dashboard-content">
        <div className="info-card">
          <h3>Account Information</h3>
          <div className="info-grid">
            <div className="info-item">
              <span className="info-label">Email:</span>
              <span className="info-value">{user?.email}</span>
            </div>
            <div className="info-item">
              <span className="info-label">User ID:</span>
              <span className="info-value">{user?.id}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Roles:</span>
              <span className="info-value">
                {userRoles.length > 0 ? userRoles.join(', ') : 'No roles assigned'}
              </span>
            </div>
          </div>
        </div>

        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">📦</div>
            <div className="stat-content">
              <h4>Products</h4>
              <p className="stat-value">0</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">🏢</div>
            <div className="stat-content">
              <h4>Warehouses</h4>
              <p className="stat-value">0</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">📋</div>
            <div className="stat-content">
              <h4>Orders</h4>
              <p className="stat-value">0</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">👥</div>
            <div className="stat-content">
              <h4>Users</h4>
              <p className="stat-value">0</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
