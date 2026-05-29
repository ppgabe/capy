import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import Register from './pages/register.jsx'
import Login from './pages/login.jsx'
import Dashboard from './pages/dashboard.jsx'
import Conversation from './pages/conversation.jsx'
import './styles/landing.css'

export default function App() {
  return (
    <>
      <Navbar />
      <main className="landing" role="main">
        <div className="container">
          <Routes>
            <Route path="/" element={<Register />} />
            <Route path="/register" element={<Register />} />
            <Route path="/login" element={<Login />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/conversation" element={<Conversation />} />
          </Routes>
        </div>
      </main>
    </>
  )
}
