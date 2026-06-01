import '../styles/dashboard.css'
import { useState, useEffect, useRef, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import DashboardButton from '../components/DashboardButton'
import SkillCard from '../components/SkillCard'
import { updateSkills } from '../api/skills'
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

export default function Dashboard() {
    const navigate = useNavigate()

    // Real State for User Data
    const [user, setUser] = useState({ name: 'Loading...', id: null, email: '', picture: '' })
    const [skills, setSkills] = useState({ skillsTeach: [], skillsLearn: [] })

    // Mock State for Counts (Until you build a stats endpoint)
    const [counts] = useState({ onlineUsers: 65, matchingSkills: 23, sessionsToday: 2 })

    // Matchmaking & WebSocket State
    const [isMatchmaking, setIsMatchmaking] = useState(false)
    const [matchedData, setMatchedData] = useState(null)
    const [showMatchModal, setShowMatchModal] = useState(false)
    const [autoDeclineTimer, setAutoDeclineTimer] = useState(10)
    const stompClientRef = useRef(null)

    // Edit Profile State
    const [showEditModal, setShowEditModal] = useState(false)
    const [editUsername, setEditUsername] = useState('')

    // Skill Persistence State
    const [isSavingSkills, setIsSavingSkills] = useState(false)
    const [saveError, setSaveError] = useState('')
    const [saveSuccess, setSaveSuccess] = useState(false)

    // Demo Mode State
    const [isDemoMode, setIsDemoMode] = useState(false)
    const [demoCount, setDemoCount] = useState(20)
    const [demoStatus, setDemoStatus] = useState('')
    const [isVerifyingLlm, setIsVerifyingLlm] = useState(false)
    const [llmStatus, setLlmStatus] = useState('')

    // 1. Fetch real user profile on load
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await fetch('/api/profile/me')
                if (res.ok) {
                    const data = await res.json()
                    const displayName = data.name || data.username || data.email
                    // Map MongoDB document to UI state
                    setUser({
                        id: data.id,
                        name: displayName,
                        email: data.email || '',
                        picture: data.picture || ''
                    })
                    setEditUsername(displayName)
                    setSkills({
                        skillsTeach: data.offeredSkills || [],
                        skillsLearn: data.requestedSkills || []
                    })
                } else if (res.status === 401) {
                    navigate('/login') // Boot to login if unauthorized
                }
            } catch (err) {
                console.error("Error fetching profile", err)
            }
        }
        fetchProfile()
    }, [navigate])

    const saveSkills = useCallback(async () => {
        setIsSavingSkills(true)
        setSaveError('')
        setSaveSuccess(false)

        const updated = await updateSkills({
            offeredSkills: skills.skillsTeach,
            requestedSkills: skills.skillsLearn
        })

        if (!updated) {
            setSaveError('Failed to save skills. Please try again.')
            setIsSavingSkills(false)
            return false
        }

        setSaveSuccess(true)
        setIsSavingSkills(false)
        return true
    }, [skills.skillsTeach, skills.skillsLearn])

    const enableDemoMode = async () => {
        setDemoStatus('Seeding demo users...')
        try {
            const res = await fetch(`/api/demo/seed?count=${demoCount}`, {
                method: 'POST'
            })
            if (!res.ok) {
                throw new Error('Failed to seed demo users')
            }
            setIsDemoMode(true)
            setDemoStatus('Demo users ready')
        } catch (error) {
            console.error(error)
            setDemoStatus('Demo seed failed')
        }
    }

    const disableDemoMode = async () => {
        setDemoStatus('Clearing demo users...')
        try {
            const res = await fetch('/api/demo/clear', {
                method: 'POST'
            })
            if (!res.ok) {
                throw new Error('Failed to clear demo users')
            }
            setIsDemoMode(false)
            setDemoStatus('Demo users cleared')
        } catch (error) {
            console.error(error)
            setDemoStatus('Demo clear failed')
        }
    }

    const verifyLlm = async () => {
        setIsVerifyingLlm(true)
        setLlmStatus('Verifying LLM...')
        try {
            const res = await fetch('/api/demo/llm/verify')
            if (!res.ok) {
                throw new Error('Verification failed')
            }
            const data = await res.json()
            if (data.ok) {
                setLlmStatus(`LLM OK: ${data.detail}`)
            } else {
                setLlmStatus(`LLM issue: ${data.status}`)
            }
        } catch (error) {
            console.error(error)
            setLlmStatus('LLM verify failed')
        } finally {
            setIsVerifyingLlm(false)
        }
    }

    // 3. The Real Matchmaking Flow
    const handleToggleMatchmaking = async () => {
        if (!isMatchmaking) {
            setIsMatchmaking(true)
            try {
                const saved = await saveSkills()
                if (!saved) {
                    setIsMatchmaking(false)
                    return
                }

                const res = await fetch('/api/matchmaking/join', {
                    method: 'POST'
                })

                if (res.ok) {
                    const data = await res.json()
                    connectToQueue(data.matchQueueTopic)
                } else {
                    setIsMatchmaking(false)
                }
            } catch (err) {
                console.error("Failed to join queue:", err)
                setIsMatchmaking(false)
            }
        } else {
            await leaveQueue()
        }
    }

    // 4. WebSocket Listener for Match Results
    const connectToQueue = (topic) => {
        const socket = new SockJS('/ws-capy')
        const stompClient = Stomp.over(socket)
        stompClientRef.current = stompClient

        stompClient.connect({}, () => {
            console.log('Listening for matches on:', topic)

            stompClient.subscribe(topic, (message) => {
                const matchPair = JSON.parse(message.body)

                // Determine which user in the pair is the OTHER person
                const otherUser = matchPair.userA.id === user.id ? matchPair.userB : matchPair.userA
                const matchId = [matchPair.userA.id, matchPair.userB.id].sort().join('-')
                const otherUserName = otherUser.name || otherUser.email || 'User'
                setMatchedData({
                    matchId: matchId,
                    otherUser: {
                        name: otherUserName,
                        avatar: otherUserName.charAt(0).toUpperCase()
                    },
                    youTeachThem: Array.from(skills.skillsTeach),
                    theyTeachYou: Array.from(otherUser.offeredSkills || []),
                    compatibilityScore: Math.round(matchPair.compatibilityScore)
                })

                setShowMatchModal(true)
                setAutoDeclineTimer(10)
            })
        })
    }

    const leaveQueue = useCallback(async () => {
        setIsMatchmaking(false)
        if (stompClientRef.current) {
            stompClientRef.current.disconnect()
            stompClientRef.current = null
        }
        await fetch('/api/matchmaking/leave', {
            method: 'POST'
        }).catch(err => console.error(err))
    }, [])

    const handleDeclineMatch = useCallback(() => {
        setShowMatchModal(false)
        setAutoDeclineTimer(10)
        leaveQueue() // Kick them out of the queue so they don't get spammed
    }, [leaveQueue])

    // 2. Handle Auto-Decline Timer
    useEffect(() => {
        let interval
        let timeout

        if (showMatchModal && autoDeclineTimer > 0) {
            interval = setInterval(() => {
                setAutoDeclineTimer((prev) => prev - 1)
            }, 1000)
        } else if (autoDeclineTimer === 0 && showMatchModal) {
            timeout = setTimeout(() => handleDeclineMatch(), 0)
        }

        return () => {
            clearInterval(interval)
            clearTimeout(timeout)
        }
    }, [showMatchModal, autoDeclineTimer, handleDeclineMatch])

    // Cleanup WebSockets on dismount
    useEffect(() => {
        return () => {
            if (stompClientRef.current) leaveQueue()
        }
    }, [leaveQueue])

    return (
        <div className="dashboard-container">
            <div className="dashboard-header">
                <div className="dashboard-top">
                    <div className="dashboard-left">
                        <p className="dashboard-welcome">WELCOME BACK, {user.name.toUpperCase()}</p>
                        <h1 className="dashboard-title">Dashboard</h1>
                    </div>
                    <div className="dashboard-right">
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <DashboardButton onClick={() => setShowEditModal(true)}>Edit Profile</DashboardButton>
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
                        skills={skills.skillsTeach}
                        onSkillsChange={(nextSkills) => {
                            setSaveSuccess(false)
                            setSaveError('')
                            setSkills((prev) => ({ ...prev, skillsTeach: nextSkills }))
                        }}
                    />
                    <SkillCard
                        title="SKILLS I WANT TO LEARN"
                        accentColor="#7BAE5C"
                        skills={skills.skillsLearn}
                        onSkillsChange={(nextSkills) => {
                            setSaveSuccess(false)
                            setSaveError('')
                            setSkills((prev) => ({ ...prev, skillsLearn: nextSkills }))
                        }}
                    />
                    <div className="skill-save-row">
                        <DashboardButton onClick={saveSkills} disabled={isSavingSkills}>
                            {isSavingSkills ? 'Saving...' : 'Save Skills'}
                        </DashboardButton>
                        {saveError && <p className="skill-save-error">{saveError}</p>}
                        {saveSuccess && !saveError && <p className="skill-save-success">Saved</p>}
                    </div>
                </div>

                <div className="cards-stack">
                    <div className="card matchmaking-card">
                        <p className="card-label">MATCHMAKING STATUS</p>
                        {isMatchmaking ? (
                            <div className="loading-animation">
                                <span className="spinner"></span>
                                <p style={{marginTop: '1rem'}}>Looking for a partner...</p>
                            </div>
                        ) : (
                            <p className="card-description">
                                Toggle <strong>Go Online</strong> to enter the matchmaking queue.
                            </p>
                        )}
                        <div className="matchmaking-button-container">
                            <DashboardButton onClick={handleToggleMatchmaking}>
                                {isMatchmaking ? 'Cancel Search' : 'Go Online'}
                            </DashboardButton>
                        </div>
                        <div className="demo-controls">
                            <label htmlFor="demo-count" className="demo-label">Demo users</label>
                            <input
                                id="demo-count"
                                type="number"
                                min="1"
                                max="200"
                                value={demoCount}
                                onChange={(e) => setDemoCount(Number(e.target.value))}
                                className="demo-input"
                            />
                            <DashboardButton onClick={isDemoMode ? disableDemoMode : enableDemoMode}>
                                {isDemoMode ? 'Disable Demo' : 'Enable Demo'}
                            </DashboardButton>
                            <DashboardButton onClick={verifyLlm} disabled={isVerifyingLlm}>
                                {isVerifyingLlm ? 'Verifying...' : 'Verify LLM'}
                            </DashboardButton>
                            {demoStatus && <p className="demo-status">{demoStatus}</p>}
                            {llmStatus && <p className="demo-status">{llmStatus}</p>}
                        </div>
                    </div>
                </div>
            </div>

            {/* Match Modal */}
            {showMatchModal && matchedData && (
                <>
                    <div className="modal-backdrop" onClick={handleDeclineMatch}></div>
                    <div className="modal">
                        <div className="modal-content">
                            <h2>Match Found!</h2>
                            <p className="auto-decline-text">
                                Auto declining in <span className="timer">{autoDeclineTimer}s</span>
                            </p>

                            <div className="match-users">
                                <div className="user-profile">
                                    <div className="user-avatar">{user.name.charAt(0).toUpperCase()}</div>
                                    <p className="user-name">{user.name}</p>
                                </div>
                                <div className="exchange-arrow">↔</div>
                                <div className="user-profile">
                                    <div className="user-avatar">{matchedData.otherUser.avatar}</div>
                                    <p className="user-name">{matchedData.otherUser.name}</p>
                                </div>
                            </div>

                            <div className="match-skills-container">
                                <div className="match-skill-card">
                                    <p className="match-skill-title">YOU TEACH THEM</p>
                                    <div className="match-skills-tags">
                                        {matchedData.youTeachThem.map((skill, index) => (
                                            <span key={index} className="match-skill-tag" style={{ backgroundColor: '#C8A26B' }}>{skill}</span>
                                        ))}
                                    </div>
                                </div>
                                <div className="match-skill-card">
                                    <p className="match-skill-title">THEY TEACH YOU</p>
                                    <div className="match-skills-tags">
                                        {matchedData.theyTeachYou.map((skill, index) => (
                                            <span key={index} className="match-skill-tag" style={{ backgroundColor: '#7BAE5C' }}>{skill}</span>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            <div className="match-compatibility">
                                <p className="compatibility-label">Compatibility Score</p>
                                <p className="compatibility-score">{matchedData.compatibilityScore} pts</p>
                            </div>

                            <div className="match-progress-container">
                                <div className="match-progress-bar" style={{ width: `${(autoDeclineTimer / 10) * 100}%` }}></div>
                            </div>

                            <div className="match-buttons">
                                <button
                                    className="btn-accept"
                                    onClick={() => navigate('/conversation', { state: { user, matchedData, matchId: matchedData.matchId } })}>
                                    Accept Match
                                </button>
                                <button className="btn-decline" onClick={handleDeclineMatch}>Decline</button>
                            </div>
                        </div>
                    </div>
                </>
            )}

            {/* Edit Profile Modal */}
            {showEditModal && (
                <>
                    <div className="modal-backdrop" onClick={() => setShowEditModal(false)}></div>
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
                                <button className="btn-accept" onClick={() => setShowEditModal(false)}>Save</button>
                                <button className="btn-decline" onClick={() => setShowEditModal(false)}>Cancel</button>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    )
}
