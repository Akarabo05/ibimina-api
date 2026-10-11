import { useState } from 'react';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { Field, Notice, Page, Pager, Pill, Table, dateTime, money, useLoad } from '../components/ui.jsx';

const today = () => new Date().toISOString().slice(0, 10);

/** Staff see everything and can record; a member sees only their own slips (mine = true). */
export default function Contributions({ mine = false }) {
    const { user } = useAuth();
    const [page, setPage] = useState(0);
    const list = useLoad(
        () => api.get(`${mine ? '/me/contributions' : '/contributions'}?page=${page}&size=15`),
        [mine, page]
    );
    const members = useLoad(() => (mine ? Promise.resolve(null) : api.get('/members?size=200')), [mine]);
    const [form, setForm] = useState({ memberId: '', amount: '', contributionDate: today() });
    const [error, setError] = useState('');
    const [done, setDone] = useState('');

    const record = async (e) => {
        e.preventDefault();
        setError('');
        setDone('');
        try {
            const c = await api.post('/contributions', { ...form, memberId: Number(form.memberId), amount: Number(form.amount) });
            setDone(`Recorded ${money(c.amount)} from ${c.memberName}. Receipt ${c.receiptNumber}.`);
            setForm({ ...form, amount: '' });
            setPage(0);
            list.reload();
        } catch (err) {
            setError(err.message);
        }
    };

    const reverse = async (c) => {
        const reason = window.prompt(`Why is receipt ${c.receiptNumber} being reversed? (min. 5 characters)`);
        if (!reason) return;
        try {
            await api.post(`/contributions/${c.id}/reverse`, { reason });
            list.reload();
        } catch (err) {
            setError(err.message);
        }
    };

    const columns = [
        { label: 'Receipt', render: (c) => <span className="receipt">{c.receiptNumber}</span> },
        ...(mine ? [] : [{ label: 'Member', render: (c) => c.memberName }]),
        { label: 'Date', render: (c) => c.contributionDate },
        { label: 'Amount', num: true, render: (c) => money(c.amount) },
        { label: 'Recorded by', render: (c) => <>{c.recordedBy}<small className="sub">{dateTime(c.recordedAt)}</small></> },
        { label: 'Status', render: (c) => (c.status === 'REVERSED' ? <><Pill value="REVERSED" /><small className="sub">{c.reversalReason}</small></> : <Pill value="ACTIVE" />) },
        ...(user.role === 'ADMIN'
            ? [{ label: '', render: (c) => c.status === 'ACTIVE' && <button className="ghost small" onClick={() => reverse(c)}>Reverse</button> }]
            : []),
    ];

    return (
        <Page
            title={mine ? 'My savings' : 'Contributions'}
            hint={mine ? 'Every payment recorded for you, with its receipt number.' : 'Entries are permanent. A mistake is reversed with a reason, never erased.'}
        >
            {!mine && (
                <form className="card form-row" onSubmit={record}>
                    <Field label="Member">
                        <select required value={form.memberId} onChange={(e) => setForm({ ...form, memberId: e.target.value })}>
                            <option value="">Choose…</option>
                            {members.data?.content.filter((m) => m.active).map((m) => <option key={m.id} value={m.id}>{m.fullName} · {m.groupName}</option>)}
                        </select>
                    </Field>
                    <Field label="Amount (RWF)">
                        <input type="number" required min="100" step="50" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} />
                    </Field>
                    <Field label="Paid on">
                        <input type="date" required max={today()} value={form.contributionDate} onChange={(e) => setForm({ ...form, contributionDate: e.target.value })} />
                    </Field>
                    <button className="primary">Record contribution</button>
                </form>
            )}
            <Notice>{error || list.error}</Notice>
            <Notice kind="ok">{done}</Notice>
            <Table columns={columns} rows={list.data?.content} empty="No contributions recorded yet." />
            <Pager data={list.data} onPage={setPage} />
        </Page>
    );
}