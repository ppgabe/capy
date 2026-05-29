// WebSocket connection for real-time messaging
let stompClient = null

export const connectWebSocket = (matchId, onMessageReceived) => {
  const SockJS = window.SockJS
  const Stomp = window.Stomp

  if (!SockJS || !Stomp) {
    console.error('SockJS or Stomp libraries not loaded')
    return
  }

  const socket = new SockJS('/ws')
  stompClient = Stomp.over(socket)

  stompClient.connect({}, (frame) => {
    console.log('Connected: ' + frame.server)

    // Subscribe to chat messages for this match
    stompClient.subscribe(`/topic/chat/${matchId}`, (message) => {
      if (onMessageReceived) {
        onMessageReceived(JSON.parse(message.body))
      }
    })
  })
}

export const disconnectWebSocket = () => {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect(() => {
      console.log('Disconnected from WebSocket')
    })
  }
}

export const sendMessage = (matchId, senderId, content) => {
  if (stompClient && stompClient.connected) {
    const message = {
      senderId,
      content,
      timestamp: new Date().toISOString(),
    }
    stompClient.send(`/app/chat/${matchId}`, {}, JSON.stringify(message))
  } else {
    console.error('WebSocket not connected')
  }
}

// Mock function to get conversation history (can be replaced with actual API call)
export const getConversationHistory = (matchId) => {
  // TODO: Replace with actual API call to backend when endpoint is available
  return []
}

// Mock function to end a conversation session
export const endConversationSession = async (matchId) => {
  try {
    // TODO: Replace with actual API call to backend
    // const response = await fetch(`/api/matches/${matchId}/end`, {
    //   method: 'POST',
    //   headers: {
    //     'Content-Type': 'application/json',
    //   },
    // })
    // return response.json()

    // For now, just disconnect WebSocket
    disconnectWebSocket()
    return { success: true }
  } catch (error) {
    console.error('Error ending conversation:', error)
    return { success: false, error }
  }
}

// Mock function to get matched user's conversation data
export const getConversationData = (matchId) => {
  // TODO: Replace with actual API call when backend endpoint is ready
  return {
    matchId,
    messages: [],
    participants: [],
  }
}
