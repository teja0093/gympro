import { Routes, Route, Link, Navigate, useNavigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Plans from "./pages/Plans.jsx";
import Profile from "./pages/Profile.jsx";
import Subscription from "./pages/Subscription.jsx";
import Attendance from "./pages/Attendance.jsx";

function Protected({ children }) {
  return localStorage.getItem("token") ? children : <Navigate to="/login" />;
}

export default function App() {
  const navigate = useNavigate();
  const email = localStorage.getItem("email");

  const logout = () => {
    localStorage.clear();
    navigate("/login");
  };

  return (
    <>
      <div className="nav">
        <b>GymPro</b>
        {email && (
          <>
            <Link to="/profile">Profile</Link>
            <Link to="/plans">Plans</Link>
            <Link to="/subscription">My Subscription</Link>
            <Link to="/attendance">Attendance</Link>
          </>
        )}
        <span style={{ marginLeft: "auto" }}>{email}</span>
        {email && <button onClick={logout}>Logout</button>}
      </div>
      <div className="container">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/profile" element={<Protected><Profile /></Protected>} />
          <Route path="/plans" element={<Protected><Plans /></Protected>} />
          <Route path="/subscription" element={<Protected><Subscription /></Protected>} />
          <Route path="/attendance" element={<Protected><Attendance /></Protected>} />
          <Route path="*" element={<Navigate to="/profile" />} />
        </Routes>
      </div>
    </>
  );
}