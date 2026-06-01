import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import {
    connectWebSocket,
    disconnectWebSocket,
    sendMessage,
    endConversationSession,
} from '../api/conversation'
import '../styles/conversation.css'

export default function Conversation() {
    const navigate = useNavigate()
    const location = useLocation()

    // Pull the real user and match data passed from the dashboard router
    const { user, matchedData, matchId } = location.state || {}

    const [messages, setMessages] = useState([])
    const [inputValue, setInputValue] = useState('')
    const [showEndModal, setShowEndModal] = useState(false)

    // Boot them back to the dashboard if they refresh the page and lose the state
    useEffect(() => {
        if (!user || !matchedData || !matchId) {
            navigate('/dashboard')
        }
    }, [user, matchedData, matchId, navigate])

    // Initialize WebSocket connection
    useEffect(() => {
        if (!matchId) return

        const handleMessageReceived = (message) => {
            const formattedMessage = {
                // Use the server timestamp as a unique key
                id: `${message.timestamp}-${message.senderId}`,
                sender: message.senderId === user.id ? 'me' : 'other',
                text: message.content,
                timestamp: new Date(message.timestamp).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit',
                }),
            }
            setMessages((prevMessages) => [...prevMessages, formattedMessage])
        }

        connectWebSocket(matchId, handleMessageReceived)

        return () => {
            disconnectWebSocket()
        }
    }, [matchId, user?.id])

    const handleSendMessage = () => {
        if (inputValue.trim() === '' || !matchId) return

        // Send message to the backend. We DO NOT add it to the local UI state here.
        // The backend stamps it and broadcasts it back to us via the WebSocket listener.
        sendMessage(matchId, user.id, inputValue)
        setInputValue('')
    }

    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault()
            handleSendMessage()
        }
    }

    const confirmEndSession = async () => {
        await endConversationSession()
        setShowEndModal(false)
        navigate('/dashboard')
    }

    if (!user || !matchedData) return null // Prevent rendering errors before redirect kicks in

    return (
        <div className="conversation-container">
            <div className="conversation-card">
                {/* Header */}
                <div className="conversation-header">
                    <div className="user-info">
                        <div className="user-avatar-large">{matchedData.otherUser.avatar}</div>
                        <div className="user-details">
                            <h2 className="user-name-large">{matchedData.otherUser.name}</h2>
                            <p className="user-status"><span className="status-indicator"></span>Active Now</p>
                        </div>
                    </div>
                    <button className="btn-end-session" onClick={() => setShowEndModal(true)}>
                        End Session
                    </button>
                </div>

                {/* Skills Section */}
                <div className="skills-section">
                    <div className="skills-container">
                        <div className="skill-group">
                            <span className="skill-group-label">YOU TEACH:</span>
                            <div className="skills-tags">
                                {matchedData.youTeachThem.map((skill, index) => (
                                    <span key={index} className="skill-tag" style={{ backgroundColor: '#C8A26B' }}>
                    {skill}
                  </span>
                                ))}
                            </div>
                        </div>
                        <div className="skill-group">
                            <span className="skill-group-label">YOU LEARN:</span>
                            <div className="skills-tags">
                                {matchedData.theyTeachYou.map((skill, index) => (
                                    <span key={index} className="skill-tag" style={{ backgroundColor: '#7BAE5C' }}>
                    {skill}
                  </span>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>

                {/* Chat Messages */}
                <div className="chat-messages">
                    {messages.map((message) => (
                        <div key={message.id} className={`message ${message.sender}`}>
                            <p className="message-text">{message.text}</p>
                            <p className="message-time">{message.timestamp}</p>
                        </div>
                    ))}
                </div>

                {/* Chat Input */}
                <div className="chat-input-container">
          <textarea
              className="chat-input"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Type a Message..."
              rows={2}
          ></textarea>
                    <button className="btn-send" onClick={handleSendMessage} aria-label="Send message">
                        <img src="/images/send-icon.png" alt="Send" />
                    </button>
                </div>
            </div>

            {/* End Session Modal */}
            {showEndModal && (
                <>
                    <div className="modal-backdrop" onClick={() => setShowEndModal(false)}></div>
                    <div className="modal">
                        <div className="modal-content">
                            <h2>End Session?</h2>
                            <p>Are you sure you want to end this conversation session?</p>
                            <div className="modal-buttons">
                                <button className="btn-cancel" onClick={() => setShowEndModal(false)}>
                                    Cancel
                                </button>
                                <button className="btn-confirm" onClick={confirmEndSession}>
                                    End Session
                                </button>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    )
}
