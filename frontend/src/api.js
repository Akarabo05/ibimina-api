const KEY = 'ibimina.token';
export const getToken = () => sessionStorage.getItem(KEY);
export const setToken = (t) => (t ? sessionStorage.setItem(KEY, t) : sessionStorage.removeItem(KEY));

export class ApiError extends Error {
    constructor(message, status) {
        super(message);
        this.status = status;
    }
}

async function request(path, { method = 'GET', body } = {}) {
    const headers = {};
    const token = getToken();
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (token) headers.Authorization = `Bearer ${token}`;

    let res;
    try {
        res = await fetch(`/api${path}`, {
            method,
            headers,
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    } catch {
        throw new ApiError('Cannot reach the server. Check your connection and try again.', 0);
    }

    const text = await res.text();
    let data = null;
    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = null;
    }

    if (!res.ok) {
        if (res.status === 401 && token) {
            setToken(null);
            window.location.assign('/login');
        }
        // The API answers {"error": "..."} or {"field": "message", ...} for validation problems
        const message = data?.error ?? (data ? Object.values(data).join('. ') : `Request failed (${res.status})`);
        throw new ApiError(message, res.status);
    }
    return data;
}

export const api = {
    get: (p) => request(p),
    post: (p, body = {}) => request(p, { method: 'POST', body }),
    put: (p, body) => request(p, { method: 'PUT', body }),
    patch: (p) => request(p, { method: 'PATCH' }),
};