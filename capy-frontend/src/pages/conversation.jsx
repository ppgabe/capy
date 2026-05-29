import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getMatchedData } from '../api/matched'
import {
  connectWebSocket,
  disconnectWebSocket,
  sendMessage,
  endConversationSession,
} from '../api/conversation'
import '../styles/conversation.css'

export default function Conversation() {
  const navigate = useNavigate()
  const matchedData = getMatchedData()
  const [messages, setMessages] = useState([])
  const [inputValue, setInputValue] = useState('')
  const [showEndModal, setShowEndModal] = useState(false)

  // Initialize WebSocket connection
  useEffect(() => {
    const handleMessageReceived = (message) => {
      const formattedMessage = {
        id: messages.length + 1,
        sender: message.senderId === matchedData.user1.id ? 'me' : 'other',
        text: message.content,
        timestamp: new Date(message.timestamp).toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit',
        }),
      }
      setMessages((prevMessages) => [...prevMessages, formattedMessage])
    }

    connectWebSocket(matchedData.user1.id, handleMessageReceived)

    return () => {
      disconnectWebSocket()
    }
  }, [])

  const handleSendMessage = () => {
    if (inputValue.trim() === '') return

    // Send message through WebSocket
    sendMessage(matchedData.user1.id, matchedData.user1.id, inputValue)

    // Add message to local state
    const newMessage = {
      id: messages.length + 1,
      sender: 'me',
      text: inputValue,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    }

    setMessages([...messages, newMessage])
    setInputValue('')
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  const confirmEndSession = async () => {
    await endConversationSession(matchedData.user1.id)
    setShowEndModal(false)
    navigate('/dashboard')
  }

  return (
    <div className="conversation-container">
      <div className="conversation-card">
        {/* Header */}
        <div className="conversation-header">
          <div className="user-info">
            <div className="user-avatar-large">{matchedData.user2.avatar}</div>
            <div className="user-details">
              <h2 className="user-name-large">{matchedData.user2.name}</h2>
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
