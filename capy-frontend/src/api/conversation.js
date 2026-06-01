import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

let stompClient = null

export const connectWebSocket = (matchId, onMessageReceived) => {
    const socket = new SockJS('/ws-capy')
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
        // Points to the @MessageMapping in ChatController
        stompClient.send(`/app/chat/${matchId}`, {}, JSON.stringify(message))
    } else {
        console.error('WebSocket not connected')
    }
}

export const endConversationSession = async () => {
    try {
        disconnectWebSocket()
        return { success: true }
    } catch (error) {
        console.error('Error ending conversation:', error)
        return { success: false, error }
    }
}
