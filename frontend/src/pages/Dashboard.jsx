import { useEffect, useState } from 'react';
import { api } from '../api.js';
import { Field, Notice, Page, Table, money, useLoad } from '../components/ui.jsx';

export default function Dashboard() {
    const groups = useLoad(() => api.get('/groups'));
    const [groupId, setGroupId] = useState('');
    const [month, setMonth] = useState(new Date().toISOString().slice(0, 7));

    useEffect(() => {
        if (!groupId && groups.data?.length) setGroupId(String(groups.data[0].id));
    }, [groups.data, groupId]);

    const summary = useLoad(
        () => (groupId ? api.get(`/groups/${groupId}/summary?month=${month}`) : Promise.resolve(null)),
        [groupId, month]
    );
    const s = summary.data;

    return (
        <Page title="Overview" hint="How much was collected this month, and who still owes.">
            <Notice>{groups.error || summary.error}</Notice>
            <div className="toolbar">
                <Field label="Group">
                    <select value={groupId} onChange={(e) => setGroupId(e.target.value)}>
                        {groups.data?.map((g) => <option key={g.id} value={g.id}>{g.groupName}</option>)}
                    </select>
                </Field>
                <Field label="Month">
                    <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
                </Field>
            </div>

            {groups.data?.length === 0 && <p className="empty">Create a group first, then add members to it.</p>}

            {s && (
                <>
                    <div className="collected">
                        <p className="big">{money(s.collectedInMonth)}</p>
                        <p>collected of {money(s.expectedInMonth)} expected from {s.activeMembers} active members</p>
                        <div className="bar" role="img" aria-label={`${s.collectionRatePercent}% collected`}>
                            <div style={{ width: `${Math.min(100, s.collectionRatePercent)}%` }} />
                        </div>
                        <p className="small">{s.collectionRatePercent}% of the month's target · {money(s.totalCollected)} saved in total</p>
                    </div>

                    <h2>Not yet paid this month</h2>
                    <Table
                        empty="Everyone has paid. Nothing is outstanding."
                        columns={[
                            { label: 'Member', render: (m) => m.fullName },
                            { label: 'Phone', render: (m) => m.phoneNumber },
                        ]}
                        rows={s.unpaidMembers}
                    />
                </>
            )}
        </Page>
    );
}