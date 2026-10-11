import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// In dev the API (and the Google OAuth2 handshake) are proxied, so the browser only talks to :5173.
const api = 'http://localhost:8085';
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': api, '/oauth2': api, '/login/oauth2': api },
  },
});
