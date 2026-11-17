import { Routes, Route, Link, Navigate, useNavigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Plans from "./pages/Plans.jsx";

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
        {email && <Link to="/plans">Plans</Link>}
        <span style={{ marginLeft: "auto" }}>{email}</span>
        {email && <button onClick={logout}>Logout</button>}
      </div>
      <div className="container">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/plans" element={<Protected><Plans /></Protected>} />
          <Route path="*" element={<Navigate to="/plans" />} />
        </Routes>
      </div>
    </>
  );
}