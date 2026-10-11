import { useState } from 'react';
import { api } from '../api.js';
import { useAuth } from '../auth.jsx';
import { Field, Notice, Page, Pager, Pill, Table, money, useLoad } from '../components/ui.jsx';

export default function Loans({ mine = false }) {
    const { user } = useAuth();
    const [page, setPage] = useState(0);
    const list = useLoad(() => api.get(`${mine ? '/me/loans' : '/loans'}?page=${page}&size=15`), [mine, page]);
    const members = useLoad(() => (mine ? Promise.resolve(null) : api.get('/members?size=200')), [mine]);
    const [form, setForm] = useState({ memberId: '', amount: '', interestRate: '5', dueDate: '' });
    const [error, setError] = useState('');

    const run = async (fn) => {
        setError('');
        try {
            await fn();
            list.reload();
        } catch (err) {
            setError(err.message);
        }
    };

    const request = (e) => {
        e.preventDefault();
        run(async () => {
            await api.post('/loans', { ...form, memberId: Number(form.memberId), amount: Number(form.amount), interestRate: Number(form.interestRate) });
            setForm({ ...form, amount: '' });
        });
    };

    const repay = (loan) => {
        const amount = window.prompt(`Repayment amount in RWF (outstanding ${money(loan.outstanding)})`);
        if (amount) run(() => api.post(`/loans/${loan.id}/repayments`, { amount: Number(amount) }));
    };

    const columns = [
        ...(mine ? [] : [{ label: 'Member', render: (l) => l.memberName }]),
        { label: 'Borrowed', num: true, render: (l) => money(l.principal) },
        { label: 'To repay', num: true, render: (l) => money(l.totalDue) },
        { label: 'Outstanding', num: true, render: (l) => money(l.outstanding) },
        { label: 'Due', render: (l) => l.dueDate },
        { label: 'Status', render: (l) => <Pill value={l.status} /> },
        ...(mine
            ? []
            : [{
                label: '',
                render: (l) => (
                    <span className="actions">
              {l.status === 'REQUESTED' && user.role === 'ADMIN' && (
                  <>
                      <button className="primary small" onClick={() => run(() => api.post(`/loans/${l.id}/approve`))}>Approve</button>
                      <button className="ghost small" onClick={() => run(() => api.post(`/loans/${l.id}/reject`))}>Reject</button>
                  </>
              )}
                        {l.status === 'APPROVED' && <button className="ghost small" onClick={() => repay(l)}>Record repayment</button>}
            </span>
                ),
            }]),
    ];

    return (
        <Page
            title={mine ? 'My loans' : 'Loans'}
            hint={mine ? '' : 'A member can borrow up to three times their savings. The admin approves, the treasurer records repayments.'}
        >
            {!mine && (
                <form className="card form-row" onSubmit={request}>
                    <Field label="Member">
                        <select required value={form.memberId} onChange={(e) => setForm({ ...form, memberId: e.target.value })}>
                            <option value="">Choose…</option>
                            {members.data?.content.filter((m) => m.active).map((m) => <option key={m.id} value={m.id}>{m.fullName}</option>)}
                        </select>
                    </Field>
                    <Field label="Amount (RWF)">
                        <input type="number" required min="1000" step="500" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} />
                    </Field>
                    <Field label="Interest (%)">
                        <input type="number" required min="0" max="100" step="0.5" value={form.interestRate} onChange={(e) => setForm({ ...form, interestRate: e.target.value })} />
                    </Field>
                    <Field label="Repay by">
                        <input type="date" required value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
                    </Field>
                    <button className="primary">Request loan</button>
                </form>
            )}
            <Notice>{error || list.error}</Notice>
            <Table columns={columns} rows={list.data?.content} empty="No loans yet." />
            <Pager data={list.data} onPage={setPage} />
        </Page>
    );
}