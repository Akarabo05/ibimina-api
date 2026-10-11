import { useState } from 'react';
import { api } from '../api.js';
import { Field, Notice, Page, Pager, Table, dateTime, useLoad } from '../components/ui.jsx';

const TYPES = ['Contribution', 'Loan', 'Member', 'Group', 'AppUser'];

export default function Audit() {
    const [page, setPage] = useState(0);
    const [type, setType] = useState('');
    const logs = useLoad(() => api.get(`/audit-logs?page=${page}&size=25${type ? `&entityType=${type}` : ''}`), [page, type]);

    return (
        <Page title="Audit trail" hint="Every sign-in and every change to money or members, with who did it and when. Stored separately from the main records.">
            <div className="toolbar">
                <Field label="Show">
                    <select value={type} onChange={(e) => { setType(e.target.value); setPage(0); }}>
                        <option value="">Everything</option>
                        {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                    </select>
                </Field>
            </div>
            <Notice>{logs.error}</Notice>
            <Table
                columns={[
                    { label: 'When', render: (l) => dateTime(l.timestamp) },
                    { label: 'Who', render: (l) => l.actor },
                    { label: 'Action', render: (l) => l.action.replaceAll('_', ' ').toLowerCase() },
                    { label: 'Details', render: (l) => l.details },
                ]}
                rows={logs.data?.content}
                empty="No activity recorded yet."
            />
            <Pager data={logs.data} onPage={setPage} />
        </Page>
    );
}