import { useEffect } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

/** Receives the JWT from the Google sign-in redirect (…/auth/callback#token=…). */
export default function AuthCallback() {
    const { acceptToken } = useAuth();
    const navigate = useNavigate();
    const token = new URLSearchParams(window.location.hash.slice(1)).get('token');

    useEffect(() => {
        if (!token) return;
        acceptToken(token);
        window.history.replaceState(null, '', '/auth/callback'); // keep the token out of history
        navigate('/', { replace: true });
    }, [token, acceptToken, navigate]);

    return token ? <p className="empty">Signing you in…</p> : <Navigate to="/login" replace />;
}