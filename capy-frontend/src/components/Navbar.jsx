import { NavLink } from 'react-router-dom'

export default function Navbar() {
  return (
    <header className="site-header">
      <div className="container">
        <div className="nav-shell">
          <NavLink to="/" className="brand" aria-label="Capy home">
            <img
              className="brand__icon"
              src="/capy-logo.png"
              alt="Capy logo"
              width="36"
              height="36"
            />
            <span className="brand__text">Capy</span>
          </NavLink>

          <div className="nav-links-container">
            <nav className="nav" aria-label="Primary">
              <ul className="nav__list" role="list">
                <li>
                  <NavLink className={({ isActive }) => `nav__link ${isActive ? 'nav__link--active' : ''}`} to="/login">
                    Register/Login
                  </NavLink>
                </li>
                <li>
                  <NavLink className={({ isActive }) => `nav__link ${isActive ? 'nav__link--active' : ''}`} to="/dashboard">
                    Dashboard
                  </NavLink>
                </li>
                <li>
                  <NavLink className={({ isActive }) => `nav__link ${isActive ? 'nav__link--active' : ''}`} to="/conversation">
                    Conversation
                  </NavLink>
                </li>
              </ul>
            </nav>
          </div>
        </div>
      </div>
    </header>
  )
}
