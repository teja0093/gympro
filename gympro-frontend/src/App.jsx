import { Routes, Route, Link, Navigate, useNavigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Plans from "./pages/Plans.jsx";
import Profile from "./pages/Profile.jsx";
import Subscription from "./pages/Subscription.jsx";
import Attendance from "./pages/Attendance.jsx";
import Workouts from "./pages/Workouts.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import ManagePlans from "./pages/ManagePlans.jsx";
import Trainer from "./pages/Trainer.jsx";
import TrainerPlans from "./pages/TrainerPlans.jsx";

function Protected({ children, roles }) {
  if (!sessionStorage.getItem("token")) return <Navigate to="/login" />;
  const role = sessionStorage.getItem("role");
  if (roles && !roles.includes(role)) return <Navigate to="/" />;
  return children;
}

// "/" sends you to the right page for your role, or to login
function Home() {
  if (!sessionStorage.getItem("token")) return <Navigate to="/login" />;
  const role = sessionStorage.getItem("role");
  return <Navigate to={role === "ADMIN" ? "/dashboard" : role === "TRAINER" ? "/trainer" : "/plans"} />;
}

export default function App() {
  const navigate = useNavigate();
  const email = sessionStorage.getItem("email");
  const role = sessionStorage.getItem("role");

  const logout = () => {
    sessionStorage.clear();
    navigate("/login");
  };

  return (
    <>
      <div className="nav">
        <b>GymPro</b>
        {email && role === "MEMBER" && (
          <>
            <Link to="/profile">Profile</Link>
            <Link to="/plans">Plans</Link>
            <Link to="/subscription">My Subscription</Link>
            <Link to="/attendance">Attendance</Link>
            <Link to="/workouts">Workouts</Link>
          </>
        )}
        {email && role === "TRAINER" && (
          <>
            <Link to="/trainer">Create Plans</Link>
            <Link to="/trainer-plans">Edit Plans</Link>
          </>
        )}
        {email && role === "ADMIN" && (
          <>
            <Link to="/dashboard">Dashboard</Link>
            <Link to="/manage-plans">Manage Plans</Link>
            <Link to="/trainer">Create Plans</Link>
            <Link to="/trainer-plans">Edit Plans</Link>
          </>
        )}
        <span style={{ marginLeft: "auto" }}>{email && `${email} (${role})`}</span>
        {email && <button onClick={logout}>Logout</button>}
      </div>
      <div className="container">
        <Routes>
          <Route path="/workouts" element={<Protected roles={["MEMBER"]}><Workouts /></Protected>} />
          <Route path="/login" element={<Login />} />
          <Route path="/profile" element={<Protected roles={["MEMBER"]}><Profile /></Protected>} />
          <Route path="/plans" element={<Protected><Plans /></Protected>} />
          <Route path="/subscription" element={<Protected roles={["MEMBER"]}><Subscription /></Protected>} />
          <Route path="/attendance" element={<Protected roles={["MEMBER"]}><Attendance /></Protected>} />
          <Route path="/workouts" element={<Protected roles={["MEMBER"]}><Workouts /></Protected>} />
          <Route path="/dashboard" element={<Protected roles={["ADMIN"]}><Dashboard /></Protected>} />
          <Route path="/manage-plans" element={<Protected roles={["ADMIN"]}><ManagePlans /></Protected>} />
          <Route path="/trainer" element={<Protected roles={["TRAINER", "ADMIN"]}><Trainer /></Protected>} />
          <Route path="/trainer-plans" element={<Protected roles={["TRAINER", "ADMIN"]}><TrainerPlans /></Protected>} />
          <Route path="*" element={<Home />} />
        </Routes>
      </div>
    </>
  );
}