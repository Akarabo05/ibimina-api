import { useState } from 'react';
import { api } from '../api.js';
import { Field, Notice, Page, Table, money, useLoad } from '../components/ui.jsx';

export default function Groups() {
    const groups = useLoad(() => api.get('/groups'));
    const [form, setForm] = useState({ groupName: '', meetingFrequency: 'MONTHLY', contributionAmount: '' });
    const [error, setError] = useState('');

    const submit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await api.post('/groups', { ...form, contributionAmount: Number(form.contributionAmount) });
            setForm({ groupName: '', meetingFrequency: 'MONTHLY', contributionAmount: '' });
            groups.reload();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <Page title="Groups" hint="Each ikimina has its own meeting rhythm and contribution amount.">
            <form className="card form-row" onSubmit={submit}>
                <Notice>{error}</Notice>
                <Field label="Group name">
                    <input required minLength={2} maxLength={60} value={form.groupName} onChange={(e) => setForm({ ...form, groupName: e.target.value })} />
                </Field>
                <Field label="Meets">
                    <select value={form.meetingFrequency} onChange={(e) => setForm({ ...form, meetingFrequency: e.target.value })}>
                        <option value="WEEKLY">Weekly</option>
                        <option value="BIWEEKLY">Every two weeks</option>
                        <option value="MONTHLY">Monthly</option>
                    </select>
                </Field>
                <Field label="Contribution (RWF)">
                    <input type="number" required min="100" step="50" value={form.contributionAmount} onChange={(e) => setForm({ ...form, contributionAmount: e.target.value })} />
                </Field>
                <button className="primary">Add group</button>
            </form>
            <Notice>{groups.error}</Notice>
            <Table
                columns={[
                    { label: 'Group', render: (g) => g.groupName },
                    { label: 'Meets', render: (g) => g.meetingFrequency.toLowerCase() },
                    { label: 'Contribution', num: true, render: (g) => money(g.contributionAmount) },
                ]}
                rows={groups.data}
                empty="No groups yet. Add the first one above."
            />
        </Page>
    );
}