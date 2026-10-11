import { useState } from 'react';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { Field, Notice, Page, Pager, Pill, Table, useLoad } from '../components/ui.jsx';

const EMPTY = { fullName: '', phoneNumber: '', email: '', groupId: '' };

export default function Members() {
    const { user } = useAuth();
    const groups = useLoad(() => api.get('/groups'));
    const [page, setPage] = useState(0);
    const [filter, setFilter] = useState('');
    const members = useLoad(
        () => api.get(`/members?page=${page}&size=15${filter ? `&groupId=${filter}` : ''}`),
        [page, filter]
    );
    const [form, setForm] = useState(EMPTY);
    const [error, setError] = useState('');

    const submit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await api.post('/members', { ...form, groupId: Number(form.groupId) });
            setForm(EMPTY);
            members.reload();
        } catch (err) {
            setError(err.message);
        }
    };

    const toggle = async (m) => {
        try {
            await api.patch(`/members/${m.id}/active?value=${!m.active}`);
            members.reload();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <Page title="Members" hint="Members are never deleted, only deactivated, so their history stays on file.">
            <form className="card form-row" onSubmit={submit}>
                <Notice>{error}</Notice>
                <Field label="Full name">
                    <input required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
                </Field>
                <Field label="Phone (07XXXXXXXX)">
                    <input required inputMode="numeric" pattern="07[0-9]{8}" value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} />
                </Field>
                <Field label="Email (optional)">
                    <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
                </Field>
                <Field label="Group">
                    <select required value={form.groupId} onChange={(e) => setForm({ ...form, groupId: e.target.value })}>
                        <option value="">Choose…</option>
                        {groups.data?.map((g) => <option key={g.id} value={g.id}>{g.groupName}</option>)}
                    </select>
                </Field>
                <button className="primary">Register member</button>
            </form>

            <div className="toolbar">
                <Field label="Show group">
                    <select value={filter} onChange={(e) => { setFilter(e.target.value); setPage(0); }}>
                        <option value="">All groups</option>
                        {groups.data?.map((g) => <option key={g.id} value={g.id}>{g.groupName}</option>)}
                    </select>
                </Field>
            </div>
            <Notice>{members.error}</Notice>
            <Table
                columns={[
                    { label: 'Name', render: (m) => m.fullName },
                    { label: 'Phone', render: (m) => m.phoneNumber },
                    { label: 'Group', render: (m) => m.groupName },
                    { label: 'Status', render: (m) => <Pill value={m.active ? 'ACTIVE' : 'INACTIVE'} /> },
                    ...(user.role === 'ADMIN'
                        ? [{ label: '', render: (m) => <button className="ghost small" onClick={() => toggle(m)}>{m.active ? 'Deactivate' : 'Reactivate'}</button> }]
                        : []),
                ]}
                rows={members.data?.content}
                empty="No members yet."
            />
            <Pager data={members.data} onPage={setPage} />
        </Page>
    );
}