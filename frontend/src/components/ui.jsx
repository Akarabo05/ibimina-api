import { useCallback, useEffect, useState } from 'react';

export const money = (n) =>
    `${Number(n ?? 0).toLocaleString('en-RW', { minimumFractionDigits: 0, maximumFractionDigits: 2 })} RWF`;

export const dateTime = (iso) => (iso ? new Date(iso).toLocaleString('en-GB', { dateStyle: 'medium', timeStyle: 'short' }) : '');

/** Loads data once and again whenever deps change. */
export function useLoad(fn, deps = []) {
    const [state, setState] = useState({ data: null, error: '', loading: true });
    const run = useCallback(() => {
        setState((s) => ({ ...s, loading: true, error: '' }));
        fn()
            .then((data) => setState({ data, error: '', loading: false }))
            .catch((e) => setState({ data: null, error: e.message, loading: false }));
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, deps);
    useEffect(run, [run]);
    return { ...state, reload: run };
}

export function Page({ title, hint, children }) {
    return (
        <section className="page">
            <header className="page-head">
                <h1>{title}</h1>
                {hint && <p className="hint">{hint}</p>}
            </header>
            {children}
        </section>
    );
}

export function Notice({ kind = 'error', children }) {
    if (!children) return null;
    return <p className={`notice ${kind}`} role={kind === 'error' ? 'alert' : 'status'}>{children}</p>;
}

export function Field({ label, children }) {
    return (
        <label className="field">
            <span>{label}</span>
            {children}
        </label>
    );
}

export function Pill({ value }) {
    return <span className={`pill ${String(value).toLowerCase()}`}>{value}</span>;
}

/** Table on desktop, stacked "slips" on phones (each cell shows its label). */
export function Table({ columns, rows, empty = 'Nothing here yet.' }) {
    if (!rows?.length) return <p className="empty">{empty}</p>;
    return (
        <div className="table-wrap">
            <table className="ledger">
                <thead>
                <tr>{columns.map((c) => <th key={c.label} className={c.num ? 'num' : ''}>{c.label}</th>)}</tr>
                </thead>
                <tbody>
                {rows.map((row, i) => (
                    <tr key={row.id ?? i} className={row.status === 'REVERSED' ? 'struck' : ''}>
                        {columns.map((c) => (
                            <td key={c.label} data-label={c.label} className={c.num ? 'num' : ''}>{c.render(row)}</td>
                        ))}
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export function Pager({ data, onPage }) {
    if (!data || data.totalPages <= 1) return null;
    return (
        <nav className="pager" aria-label="Pages">
            <button className="ghost" disabled={data.page === 0} onClick={() => onPage(data.page - 1)}>Previous</button>
            <span>Page {data.page + 1} of {data.totalPages}</span>
            <button className="ghost" disabled={data.page + 1 >= data.totalPages} onClick={() => onPage(data.page + 1)}>Next</button>
        </nav>
    );
}