import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    define: {
        global: 'window'
    },
    server: {
        proxy: {
            '/api': 'http://localhost:8080',
            '/oauth2': 'http://localhost:8080',
            // Proxy WebSockets correctly
            '/ws-capy': {
                target: 'ws://localhost:8080',
                ws: true
            }
        }
    }
})
