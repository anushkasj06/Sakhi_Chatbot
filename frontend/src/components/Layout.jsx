import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Layout.css';

const Layout = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const userRoles = user?.roles || [];
  const isAdmin = userRoles.includes('ADMIN');
  const isWarehouseManager = userRoles.includes('WAREHOUSE_MANAGER');

  const isActive = (path) => location.pathname === path;

  return (
    <div className="layout">
      <nav className="navbar">
        <div className="navbar-brand">
          <Link to="/dashboard">WMS</Link>
        </div>

        <div className="navbar-menu">
          <Link 
            to="/dashboard" 
            className={`nav-link ${isActive('/dashboard') ? 'active' : ''}`}
          >
            Dashboard
          </Link>

          <Link 
            to="/products" 
            className={`nav-link ${isActive('/products') ? 'active' : ''}`}
          >
            Products
          </Link>

          {(isAdmin || isWarehouseManager) && (
            <>
              <Link 
                to="/suppliers" 
                className={`nav-link ${isActive('/suppliers') ? 'active' : ''}`}
              >
                Suppliers
              </Link>

              <Link 
                to="/warehouses" 
                className={`nav-link ${isActive('/warehouses') ? 'active' : ''}`}
              >
                Warehouses
              </Link>
            </>
          )}

          {isAdmin && (
            <Link 
              to="/users" 
              className={`nav-link ${isActive('/users') ? 'active' : ''}`}
            >
              Users
            </Link>
          )}
        </div>

        <div className="navbar-user">
          <div className="user-info">
            <span className="user-name">{user?.email}</span>
            <span className="user-role">{userRoles[0] || 'User'}</span>
          </div>
          <button onClick={handleLogout} className="btn-logout">
            Logout
          </button>
        </div>
      </nav>

      <main className="main-content">
        {children}
      </main>
    </div>
  );
};

export default Layout;
