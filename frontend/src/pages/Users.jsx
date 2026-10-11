import { useState } from 'react';
import { api } from '../api.js';
import { Field, Notice, Page, Pill, Table, useLoad } from '../components/ui.jsx';

const EMPTY = { username: '', password: '', email: '', role: 'TREASURER', memberId: '' };

export default function Users() {
    const users = useLoad(() => api.get('/users'));
    const members = useLoad(() => api.get('/members?size=200'));
    const [form, setForm] = useState(EMPTY);
    const [error, setError] = useState('');

    const submit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await api.post('/users', { ...form, email: form.email || null, memberId: form.role === 'MEMBER' ? Number(form.memberId) : null });
            setForm(EMPTY);
            users.reload();
        } catch (err) {
            setError(err.message);
        }
    };

    const toggle = async (u) => {
        try {
            await api.patch(`/users/${u.id}/enabled?value=${!u.enabled}`);
            users.reload();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <Page title="Accounts" hint="Who may sign in, and what each role is allowed to do. A Google account can sign in once its email matches an account here.">
            <form className="card form-row" onSubmit={submit}>
                <Field label="Username">
                    <input required minLength={3} value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
                </Field>
                <Field label="Password (8+ characters)">
                    <input type="password" required minLength={8} autoComplete="new-password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
                </Field>
                <Field label="Email (needed for Google sign-in)">
                    <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
                </Field>
                <Field label="Role">
                    <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                        <option value="TREASURER">Treasurer</option>
                        <option value="ADMIN">Admin</option>
                        <option value="MEMBER">Member</option>
                    </select>
                </Field>
                {form.role === 'MEMBER' && (
                    <Field label="Linked member">
                        <select required value={form.memberId} onChange={(e) => setForm({ ...form, memberId: e.target.value })}>
                            <option value="">Choose…</option>
                            {members.data?.content.map((m) => <option key={m.id} value={m.id}>{m.fullName}</option>)}
                        </select>
                    </Field>
                )}
                <button className="primary">Create account</button>
                <Notice>{error}</Notice>
            </form>
            <Notice>{users.error}</Notice>
            <Table
                columns={[
                    { label: 'Username', render: (u) => u.username },
                    { label: 'Email', render: (u) => u.email ?? '—' },
                    { label: 'Role', render: (u) => u.role.toLowerCase() },
                    { label: 'Status', render: (u) => <Pill value={u.enabled ? 'ACTIVE' : 'DISABLED'} /> },
                    { label: '', render: (u) => <button className="ghost small" onClick={() => toggle(u)}>{u.enabled ? 'Disable' : 'Enable'}</button> },
                ]}
                rows={users.data}
            />
        </Page>
    );
}