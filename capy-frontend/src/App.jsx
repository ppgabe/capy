import { Routes, Route, useLocation } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import Login from './pages/login.jsx'
import Dashboard from './pages/dashboard.jsx'
import Conversation from './pages/conversation.jsx'
import './styles/landing.css'

export default function App() {
  const location = useLocation()
  
  return (
    <>
      <Navbar />
      <main className="landing" role="main" key={location.pathname}>
        <div className="container">
          <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/register" element={<Login />} />
            <Route path="/login" element={<Login />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/conversation" element={<Conversation />} />
          </Routes>
        </div>
      </main>
    </>
  )
}
