import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// In development, forward /api calls to the Spring Boot backend so the
// browser sees a single origin (no CORS setup needed locally).
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': process.env.VITE_API_TARGET ?? 'http://localhost:8080',
    },
  },
})
