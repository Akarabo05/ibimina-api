import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth.jsx';

const NAV = {
    ADMIN: [['/', 'Overview'], ['/groups', 'Groups'], ['/members', 'Members'], ['/contributions', 'Contributions'], ['/loans', 'Loans'], ['/users', 'Accounts'], ['/audit', 'Audit trail']],
    TREASURER: [['/', 'Overview'], ['/members', 'Members'], ['/contributions', 'Contributions'], ['/loans', 'Loans']],
    MEMBER: [['/me/contributions', 'My savings'], ['/me/loans', 'My loans']],
};

export default function Layout() {
    const { user, logout } = useAuth();
    const items = NAV[user.role] ?? [];
    return (
        <div className="shell">
            <aside className="rail">
                <div className="brand">Ibimina</div>
                <nav aria-label="Main">
                    {items.map(([to, label]) => (
                        <NavLink key={to} to={to} end={to === '/'} className={({ isActive }) => (isActive ? 'active' : '')}>
                            {label}
                        </NavLink>
                    ))}
                </nav>
                <div className="who">
                    <strong>{user.username}</strong>
                    <span>{user.role.toLowerCase()}</span>
                    <button className="ghost on-dark" onClick={logout}>Sign out</button>
                </div>
            </aside>
            <main className="main">
                <Outlet />
            </main>
        </div>
    );
}