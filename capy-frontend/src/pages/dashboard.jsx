import '../styles/dashboard.css'
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getUserData, updateUserName } from '../api/user'
import { getDashboardCounts } from '../api/dashboardCounts'
import { getInitialSkills } from '../api/skills'
import { getMatchedData } from '../api/matched'
import DashboardButton from '../components/DashboardButton'
import SkillCard from '../components/SkillCard'

export default function Dashboard() {
  const navigate = useNavigate()
  const user = getUserData()
  const counts = getDashboardCounts()
  const skills = getInitialSkills()
  const matchedData = getMatchedData()
  const [isMatchmaking, setIsMatchmaking] = useState(false)
  const [showMatchModal, setShowMatchModal] = useState(false)
  const [autoDeclineTimer, setAutoDeclineTimer] = useState(10)
  const [showEditModal, setShowEditModal] = useState(false)
  const [editUsername, setEditUsername] = useState(user.name)

  useEffect(() => {
    let interval
    if (showMatchModal && autoDeclineTimer > 0) {
      interval = setInterval(() => {
        setAutoDeclineTimer((prev) => prev - 1)
      }, 1000)
    } else if (autoDeclineTimer === 0 && showMatchModal) {
      setShowMatchModal(false)
      setAutoDeclineTimer(10)
    }
    return () => clearInterval(interval)
  }, [showMatchModal, autoDeclineTimer])

  const handleSimulateMatch = () => {
    if (isMatchmaking) {
      setShowMatchModal(true)
      setAutoDeclineTimer(10)
    }
  }

  const closeModal = () => {
    setShowMatchModal(false)
    setAutoDeclineTimer(10)
  }

  const handleEditProfile = () => {
    setShowEditModal(true)
  }

  const handleSaveUsername = () => {
    if (editUsername.trim() !== '') {
      updateUserName(editUsername)
      setShowEditModal(false)
    }
  }

  const closeEditModal = () => {
    setEditUsername(user.name)
    setShowEditModal(false)
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <div className="dashboard-top">
          <div className="dashboard-left">
            <p className="dashboard-welcome">WELCOME BACK, {user.name.toUpperCase()}</p>
            <h1 className="dashboard-title">Dashboard</h1>
          </div>
          <div className="dashboard-right">
            <DashboardButton onClick={handleSimulateMatch}>Simulate Match</DashboardButton>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <DashboardButton onClick={handleEditProfile}>Edit Profile</DashboardButton>
              <img src="/images/profile-icon.png" alt="Profile" style={{ width: '20px', height: '20px' }} />
            </div>
          </div>
        </div>
      </div>

      <div className="dashboard-cards">
        <div className="cards-stack">
          <div className="card">
            <p className="card-label">ONLINE USERS</p>
            <p className="card-count">{counts.onlineUsers}</p>
          </div>
          <div className="card">
            <p className="card-label">MATCHING YOUR SKILLS</p>
            <p className="card-count">{counts.matchingSkills}</p>
          </div>
          <div className="card">
            <p className="card-label">SESSIONS TODAY</p>
            <p className="card-count">{counts.sessionsToday}</p>
          </div>
        </div>

        <div className="cards-stack">
          <SkillCard
            title="SKILLS I TEACH"
            accentColor="#C8A26B"
            initialSkills={skills.skillsTeach}
          />
          <SkillCard
            title="SKILLS I WANT TO LEARN"
            accentColor="#7BAE5C"
            initialSkills={skills.skillsLearn}
          />
        </div>

        <div className="cards-stack">
          <div className="card matchmaking-card">
            <p className="card-label">MATCHMAKING STATUS</p>
            {isMatchmaking ? (
              <div className="loading-animation">
                <span className="spinner"></span>
              </div>
            ) : (
              <p className="card-description">
                Toggle <strong>Go Online</strong> to enter the matchmaking queue.
              </p>
            )}
            <div className="matchmaking-button-container">
              <DashboardButton onClick={() => setIsMatchmaking(!isMatchmaking)}>
                Go Online
              </DashboardButton>
            </div>
          </div>
        </div>

        <div className="cards-stack">
          <div className="card"></div>
        </div>
      </div>

      {showMatchModal && (
        <>
          <div className="modal-backdrop" onClick={closeModal}></div>
          <div className="modal">
            <div className="modal-content">
              <h2>Match Found!</h2>
              <p className="auto-decline-text">
                Auto declining in <span className="timer">{autoDeclineTimer}s</span>
              </p>

              {/* User Exchange */}
              <div className="match-users">
                <div className="user-profile">
                  <div className="user-avatar">{user.name.charAt(0).toUpperCase()}</div>
                  <p className="user-name">{user.name}</p>
                </div>
                <div className="exchange-arrow">↔</div>
                <div className="user-profile">
                  <div className="user-avatar">{matchedData.user2.avatar}</div>
                  <p className="user-name">{matchedData.user2.name}</p>
                </div>
              </div>

              {/* Skills Section */}
              <div className="match-skills-container">
                <div className="match-skill-card">
                  <p className="match-skill-title">YOU TEACH THEM</p>
                  <div className="match-skills-tags">
                    {matchedData.youTeachThem.map((skill, index) => (
                      <span key={index} className="match-skill-tag" style={{ backgroundColor: '#C8A26B' }}>
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>
                <div className="match-skill-card">
                  <p className="match-skill-title">THEY TEACH YOU</p>
                  <div className="match-skills-tags">
                    {matchedData.theyTeachYou.map((skill, index) => (
                      <span key={index} className="match-skill-tag" style={{ backgroundColor: '#7BAE5C' }}>
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>
              </div>

              {/* Compatibility Score */}
              <div className="match-compatibility">
                <p className="compatibility-label">Compatibility Score</p>
                <p className="compatibility-score">{matchedData.compatibilityScore} pts</p>
              </div>

              {/* Auto Decline Progress Bar */}
              <div className="match-progress-container">
                <div className="match-progress-bar" style={{ width: `${(autoDeclineTimer / 10) * 100}%` }}></div>
              </div>

              {/* Action Buttons */}
              <div className="match-buttons">
                <button className="btn-accept" onClick={() => navigate('/conversation')}>Accept Match</button>
                <button className="btn-decline" onClick={closeModal}>Decline</button>
              </div>
            </div>
          </div>
        </>
      )}

      {showEditModal && (
        <>
          <div className="modal-backdrop" onClick={closeEditModal}></div>
          <div className="modal">
            <div className="modal-content">
              <h2>Edit Profile</h2>
              <div className="edit-profile-section">
                <label htmlFor="username" className="edit-label">Username</label>
                <input
                  id="username"
                  type="text"
                  value={editUsername}
                  onChange={(e) => setEditUsername(e.target.value)}
                  className="edit-input"
                  placeholder="Enter your username"
                />
              </div>
              <div className="match-buttons">
                <button className="btn-accept" onClick={handleSaveUsername}>Save</button>
                <button className="btn-decline" onClick={closeEditModal}>Cancel</button>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
