import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth.jsx';
import Layout from './components/Layout.jsx';
import Login from './pages/Login.jsx';
import AuthCallback from './pages/AuthCallback.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Groups from './pages/Groups.jsx';
import Members from './pages/Members.jsx';
import Contributions from './pages/Contributions.jsx';
import Loans from './pages/Loans.jsx';
import Users from './pages/Users.jsx';
import Audit from './pages/Audit.jsx';

/** Shows the page only to the listed roles; everyone else is sent to their own home. */
function Guard({ roles, children }) {
    const { user } = useAuth();
    if (!user) return <Navigate to="/login" replace />;
    if (roles && !roles.includes(user.role)) {
        return <Navigate to={user.role === 'MEMBER' ? '/me/contributions' : '/'} replace />;
    }
    return children;
}

const STAFF = ['ADMIN', 'TREASURER'];

export default function App() {
    return (
        <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/auth/callback" element={<AuthCallback />} />
            <Route element={<Guard><Layout /></Guard>}>
                <Route path="/" element={<Guard roles={STAFF}><Dashboard /></Guard>} />
                <Route path="/groups" element={<Guard roles={['ADMIN']}><Groups /></Guard>} />
                <Route path="/members" element={<Guard roles={STAFF}><Members /></Guard>} />
                <Route path="/contributions" element={<Guard roles={STAFF}><Contributions /></Guard>} />
                <Route path="/loans" element={<Guard roles={STAFF}><Loans /></Guard>} />
                <Route path="/users" element={<Guard roles={['ADMIN']}><Users /></Guard>} />
                <Route path="/audit" element={<Guard roles={['ADMIN']}><Audit /></Guard>} />
                <Route path="/me/contributions" element={<Guard roles={['MEMBER']}><Contributions mine /></Guard>} />
                <Route path="/me/loans" element={<Guard roles={['MEMBER']}><Loans mine /></Guard>} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}