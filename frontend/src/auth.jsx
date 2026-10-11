import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { api, getToken, setToken } from './api.js';

const AuthContext = createContext(null);

function readUser(token) {
    if (!token) return null;
    try {
        const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        const claims = JSON.parse(atob(payload));
        if (claims.exp * 1000 < Date.now()) return null;
        return {
            username: claims.sub,
            role: (claims.roles?.[0] ?? '').replace('ROLE_', ''),
            memberId: claims.memberId ? Number(claims.memberId) : null,
        };
    } catch {
        return null;
    }
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => readUser(getToken()));

    const acceptToken = useCallback((token) => {
        setToken(token);
        setUser(readUser(token));
    }, []);

    const login = useCallback(async (username, password) => {
        const res = await api.post('/auth/login', { username, password });
        acceptToken(res.token);
    }, [acceptToken]);

    const logout = useCallback(() => acceptToken(null), [acceptToken]);

    const value = useMemo(() => ({ user, login, logout, acceptToken }), [user, login, logout, acceptToken]);
    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
export const isStaff = (user) => user?.role === 'ADMIN' || user?.role === 'TREASURER';